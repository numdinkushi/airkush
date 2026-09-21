package com.kush.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kush.exception.BadRequestException;
import com.kush.exception.ConflictException;
import com.kush.exception.ForbiddenException;
import com.kush.exception.ResourceNotFoundException;
import com.kush.mapper.AirlineMapper;
import com.kush.model.Airline;
import com.kush.payload.request.AirlineRequest;
import com.kush.payload.response.AirlineResponse;
import com.kush.repository.AirlineRepository;
import com.kush.repository.FlightRepository;
import com.kush.security.SecurityUtils;
import com.kush.service.AirlineService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;
    private final FlightRepository flightRepository;

    @Override
    public AirlineResponse createAirline(AirlineRequest request) {
        Long ownerUserId = resolveOwnerUserId(request);
        assertIataAvailable(request.getIataCode(), null);
        assertIcaoAvailable(request.getIcaoCode(), null);

        if (!SecurityUtils.isAdmin() && airlineRepository.existsByOwnerUserId(ownerUserId)) {
            throw new ConflictException("owner already has an airline");
        }

        Airline saved = airlineRepository.save(AirlineMapper.toEntity(request, ownerUserId));
        return AirlineMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AirlineResponse getAirlineById(Long id) {
        return AirlineMapper.toResponse(requireAirline(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AirlineResponse getAirlineByIataCode(String iataCode) {
        Airline airline = airlineRepository.findByIataCodeIgnoreCase(iataCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "airline not found with IATA code: " + iataCode));
        return AirlineMapper.toResponse(airline);
    }

    @Override
    public AirlineResponse updateAirline(Long id, AirlineRequest request) {
        Airline airline = requireAirline(id);
        assertCanManage(airline);

        if (request.getIataCode() != null) {
            assertIataAvailable(request.getIataCode(), id);
        }
        if (request.getIcaoCode() != null) {
            assertIcaoAvailable(request.getIcaoCode(), id);
        }
        if (request.getOwnerUserId() != null
                && !request.getOwnerUserId().equals(airline.getOwnerUserId())) {
            if (!SecurityUtils.isAdmin()) {
                throw new ForbiddenException("Only an admin can change an airline's owner");
            }
            airline.setOwnerUserId(request.getOwnerUserId());
        }

        AirlineMapper.applyUpdate(airline, request);
        return AirlineMapper.toResponse(airlineRepository.save(airline));
    }

    @Override
    public void deleteAirline(Long id) {
        Airline airline = requireAirline(id);
        assertCanManage(airline);
        if (flightRepository.existsByAirlineId(id)) {
            throw new ConflictException("cannot delete airline that still has flights");
        }
        airlineRepository.delete(airline);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AirlineResponse> getAllAirlines(String search, Pageable pageable) {
        Page<Airline> airlines = StringUtils.hasText(search)
                ? airlineRepository.searchByKeyword(search.trim(), pageable)
                : airlineRepository.findAll(pageable);
        return airlines.map(AirlineMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean airlineExists(String iataCode) {
        return airlineRepository.existsByIataCodeIgnoreCase(iataCode.trim());
    }

    private Airline requireAirline(Long id) {
        return airlineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("airline not found with id: " + id));
    }

    private Long resolveOwnerUserId(AirlineRequest request) {
        if (SecurityUtils.isAdmin()) {
            if (request.getOwnerUserId() == null) {
                throw new BadRequestException("Owner user id is required");
            }
            return request.getOwnerUserId();
        }
        return SecurityUtils.requireUserId();
    }

    private void assertCanManage(Airline airline) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!airline.getOwnerUserId().equals(SecurityUtils.requireUserId())) {
            throw new ForbiddenException("You can only manage your own airline");
        }
    }

    private void assertIataAvailable(String iataCode, Long currentId) {
        String code = iataCode.toUpperCase().trim();
        boolean taken = currentId == null
                ? airlineRepository.existsByIataCodeIgnoreCase(code)
                : airlineRepository.existsByIataCodeIgnoreCaseAndIdNot(code, currentId);
        if (taken) {
            throw new ConflictException("airline with given IATA code already exist");
        }
    }

    private void assertIcaoAvailable(String icaoCode, Long currentId) {
        if (!StringUtils.hasText(icaoCode)) {
            return;
        }
        String code = icaoCode.toUpperCase().trim();
        boolean taken = currentId == null
                ? airlineRepository.existsByIcaoCodeIgnoreCase(code)
                : airlineRepository.existsByIcaoCodeIgnoreCaseAndIdNot(code, currentId);
        if (taken) {
            throw new ConflictException("airline with given ICAO code already exist");
        }
    }
}
