package com.kush.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kush.client.LocationAirportClient;
import com.kush.enums.FlightStatus;
import com.kush.exception.BadRequestException;
import com.kush.exception.ConflictException;
import com.kush.exception.ForbiddenException;
import com.kush.exception.ResourceNotFoundException;
import com.kush.mapper.FlightMapper;
import com.kush.model.Airline;
import com.kush.model.Flight;
import com.kush.payload.request.FlightRequest;
import com.kush.payload.response.AirportResponse;
import com.kush.payload.response.FlightResponse;
import com.kush.repository.AirlineRepository;
import com.kush.repository.FlightRepository;
import com.kush.security.SecurityUtils;
import com.kush.service.FlightService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final LocationAirportClient locationAirportClient;

    @Override
    public FlightResponse createFlight(FlightRequest request) {
        Airline airline = requireAirline(request.getAirlineId());
        assertCanManage(airline);
        assertSchedule(request.getOriginIata(), request.getDestinationIata(),
                request.getDepartureTime(), request.getArrivalTime());
        if (request.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Departure time must be in the future");
        }

        String flightNumber = FlightMapper.upper(request.getFlightNumber());
        if (flightRepository.existsByAirlineIdAndFlightNumberIgnoreCaseAndDepartureTime(
                airline.getId(), flightNumber, request.getDepartureTime())) {
            throw new ConflictException("flight with given number and departure already exist");
        }

        AirportResponse origin = locationAirportClient.requireByIata(request.getOriginIata());
        AirportResponse destination = locationAirportClient.requireByIata(request.getDestinationIata());
        Flight saved = flightRepository.save(FlightMapper.toEntity(request, airline, origin, destination));
        return FlightMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        return FlightMapper.toResponse(requireFlight(id));
    }

    @Override
    public FlightResponse updateFlight(Long id, FlightRequest request) {
        Flight flight = requireFlight(id);
        assertCanManage(flight.getAirline());
        assertSchedule(request.getOriginIata(), request.getDestinationIata(),
                request.getDepartureTime(), request.getArrivalTime());

        if (!request.getAirlineId().equals(flight.getAirline().getId())) {
            throw new BadRequestException("Airline cannot be changed on an existing flight");
        }

        String flightNumber = FlightMapper.upper(request.getFlightNumber());
        if (flightRepository.existsByAirlineIdAndFlightNumberIgnoreCaseAndDepartureTimeAndIdNot(
                flight.getAirline().getId(), flightNumber, request.getDepartureTime(), id)) {
            throw new ConflictException("flight with given number and departure already exist");
        }

        AirportResponse origin = locationAirportClient.requireByIata(request.getOriginIata());
        AirportResponse destination = locationAirportClient.requireByIata(request.getDestinationIata());

        flight.setFlightNumber(flightNumber);
        flight.setOriginIata(origin.getIataCode());
        flight.setOriginName(origin.getName());
        flight.setDestinationIata(destination.getIataCode());
        flight.setDestinationName(destination.getName());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        if (request.getStatus() != null) {
            flight.setStatus(request.getStatus());
        }
        applySeatChange(flight, request.getTotalSeats());

        return FlightMapper.toResponse(flightRepository.save(flight));
    }

    @Override
    public void deleteFlight(Long id) {
        Flight flight = requireFlight(id);
        assertCanManage(flight.getAirline());
        if (flight.getAvailableSeats() < flight.getTotalSeats()) {
            throw new ConflictException("cannot delete a flight that has booked seats");
        }
        flightRepository.delete(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> searchFlights(
            String origin,
            String destination,
            LocalDate date,
            Long airlineId,
            FlightStatus status,
            String search,
            Pageable pageable
    ) {
        return flightRepository.search(
                normalizeIata(origin),
                normalizeIata(destination),
                airlineId,
                status,
                date,
                StringUtils.hasText(search) ? search.trim() : null,
                pageable
        ).map(FlightMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByAirline(Long airlineId, Pageable pageable) {
        requireAirline(airlineId);
        return flightRepository.findByAirlineId(airlineId, pageable).map(FlightMapper::toResponse);
    }

    private Flight requireFlight(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("flight not found with id: " + id));
    }

    private Airline requireAirline(Long id) {
        return airlineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("airline not found with id: " + id));
    }

    private void assertCanManage(Airline airline) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        if (!airline.getOwnerUserId().equals(SecurityUtils.requireUserId())) {
            throw new ForbiddenException("You can only manage flights for your own airline");
        }
    }

    private void assertSchedule(
            String originIata,
            String destinationIata,
            LocalDateTime departure,
            LocalDateTime arrival
    ) {
        if (originIata.equalsIgnoreCase(destinationIata)) {
            throw new BadRequestException("Origin and destination must be different");
        }
        if (!arrival.isAfter(departure)) {
            throw new BadRequestException("Arrival time must be after departure time");
        }
    }

    private void applySeatChange(Flight flight, Integer totalSeats) {
        if (totalSeats == null || totalSeats.equals(flight.getTotalSeats())) {
            return;
        }
        int delta = totalSeats - flight.getTotalSeats();
        flight.setTotalSeats(totalSeats);
        flight.setAvailableSeats(Math.max(0, Math.min(totalSeats, flight.getAvailableSeats() + delta)));
    }

    private static String normalizeIata(String value) {
        return StringUtils.hasText(value) ? value.toUpperCase().trim() : null;
    }
}
