package com.kush.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.kush.csv.LocationCsvMapper;
import com.kush.csv.LocationCsvMapper.AirportCsvRow;
import com.kush.csv.LocationCsvMapper.Indexed;
import com.kush.exception.ConflictException;
import com.kush.exception.ResourceNotFoundException;
import com.kush.mapper.AirportMapper;
import com.kush.model.Airport;
import com.kush.model.City;
import com.kush.payload.request.AirportRequest;
import com.kush.payload.response.AirportResponse;
import com.kush.payload.response.BulkFailure;
import com.kush.payload.response.BulkResult;
import com.kush.repository.AirportRepository;
import com.kush.repository.CityRepository;
import com.kush.service.AirportService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;
    private final CityRepository cityRepository;
    private final LocationCsvMapper csvMapper;
    private final Validator validator;

    @Override
    public AirportResponse createAirport(AirportRequest request) {
        String iataCode = request.getIataCode().toUpperCase().trim();
        String icaoCode = request.getIcaoCode().toUpperCase().trim();

        if (airportRepository.existsByIataCode(iataCode)) {
            throw new ConflictException("airport with given IATA code already exist");
        }
        if (airportRepository.existsByIcaoCode(icaoCode)) {
            throw new ConflictException("airport with given ICAO code already exist");
        }

        City city = requireCity(request.getCityId());
        Airport airport = AirportMapper.toEntity(request, city);
        Airport saved = airportRepository.save(airport);
        return AirportMapper.toResponse(saved);
    }

    @Override
    public AirportResponse getAirportById(Long id) {
        return AirportMapper.toResponse(requireAirport(id));
    }

    @Override
    public AirportResponse getAirportByIataCode(String iataCode) {
        Airport airport = airportRepository.findByIataCodeIgnoreCase(iataCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "airport not found with IATA code: " + iataCode));
        return AirportMapper.toResponse(airport);
    }

    @Override
    public AirportResponse updateAirport(Long id, AirportRequest request) {
        Airport airport = requireAirport(id);

        if (request.getIataCode() != null
                && airportRepository.existsByIataCodeAndIdNot(
                        request.getIataCode().toUpperCase().trim(), id)) {
            throw new ConflictException("airport with given IATA code already exist");
        }
        if (request.getIcaoCode() != null
                && airportRepository.existsByIcaoCodeAndIdNot(
                        request.getIcaoCode().toUpperCase().trim(), id)) {
            throw new ConflictException("airport with given ICAO code already exist");
        }

        City city = request.getCityId() != null
                ? requireCity(request.getCityId())
                : airport.getCity();

        AirportMapper.updateEntity(airport, request, city);
        Airport saved = airportRepository.save(airport);
        return AirportMapper.toResponse(saved);
    }

    @Override
    public void deleteAirport(Long id) {
        if (!airportRepository.existsById(id)) {
            throw new ResourceNotFoundException("airport not found with id: " + id);
        }
        airportRepository.deleteById(id);
    }

    @Override
    public Page<AirportResponse> getAllAirports(String search, Pageable pageable) {
        Page<Airport> airports = StringUtils.hasText(search)
                ? airportRepository.searchByKeyword(search.trim(), pageable)
                : airportRepository.findAll(pageable);
        return airports.map(AirportMapper::toResponse);
    }

    @Override
    public Page<AirportResponse> getAirportsByCityId(Long cityId, Pageable pageable) {
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("city not found with id: " + cityId);
        }
        return airportRepository.findByCityId(cityId, pageable).map(AirportMapper::toResponse);
    }

    @Override
    public boolean airportExists(String iataCode) {
        return airportRepository.existsByIataCode(iataCode.toUpperCase().trim());
    }

    @Override
    public BulkResult<AirportResponse> createAirports(List<AirportRequest> requests) {
        List<AirportResponse> created = new ArrayList<>();
        List<BulkFailure> failed = new ArrayList<>();
        Set<String> seenIata = new HashSet<>();
        Set<String> seenIcao = new HashSet<>();

        for (int i = 0; i < requests.size(); i++) {
            AirportRequest request = requests.get(i);
            int index = i + 1;
            String iata = normalize(request.getIataCode());
            String icao = normalize(request.getIcaoCode());
            if (duplicateCodes(index, iata, icao, seenIata, seenIcao, failed)) {
                continue;
            }
            saveAirport(index, iata, request, created, failed);
        }
        return BulkResult.of(created, failed, requests.size());
    }

    @Override
    public BulkResult<AirportResponse> importAirports(MultipartFile file) {
        List<Indexed<AirportCsvRow>> rows = csvMapper.toAirportRows(file);
        List<AirportResponse> created = new ArrayList<>();
        List<BulkFailure> failed = new ArrayList<>();
        Set<String> seenIata = new HashSet<>();
        Set<String> seenIcao = new HashSet<>();

        for (Indexed<AirportCsvRow> row : rows) {
            AirportCsvRow csv = row.value();
            String iata = normalize(csv.iataCode());
            String icao = normalize(csv.icaoCode());
            Long cityId = resolveCityId(csv);
            if (cityId == null) {
                failed.add(failure(row.index(), iata, "city not found; provide a valid cityId or cityCode"));
                continue;
            }
            AirportRequest request = csvMapper.toAirportRequest(csv, cityId);
            String violations = violationsOf(request);
            if (violations != null) {
                failed.add(failure(row.index(), iata, violations));
                continue;
            }
            if (duplicateCodes(row.index(), iata, icao, seenIata, seenIcao, failed)) {
                continue;
            }
            saveAirport(row.index(), iata, request, created, failed);
        }
        return BulkResult.of(created, failed, rows.size());
    }

    private Long resolveCityId(AirportCsvRow csv) {
        if (csv.cityId() != null) {
            return cityRepository.existsById(csv.cityId()) ? csv.cityId() : null;
        }
        if (!StringUtils.hasText(csv.cityCode())) {
            return null;
        }
        return cityRepository.findByCityCodeIgnoreCase(csv.cityCode().trim())
                .map(City::getId)
                .orElse(null);
    }

    private boolean duplicateCodes(
            int index,
            String iata,
            String icao,
            Set<String> seenIata,
            Set<String> seenIcao,
            List<BulkFailure> failed
    ) {
        if (iata != null && !seenIata.add(iata)) {
            failed.add(failure(index, iata, "duplicate IATA code in this request"));
            return true;
        }
        if (icao != null && !seenIcao.add(icao)) {
            failed.add(failure(index, icao, "duplicate ICAO code in this request"));
            return true;
        }
        return false;
    }

    private void saveAirport(
            int index,
            String iata,
            AirportRequest request,
            List<AirportResponse> created,
            List<BulkFailure> failed
    ) {
        try {
            created.add(createAirport(request));
        } catch (ConflictException | ResourceNotFoundException ex) {
            failed.add(failure(index, iata, ex.getMessage()));
        }
    }

    private String violationsOf(AirportRequest request) {
        var violations = validator.validate(request);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .reduce((first, second) -> first + "; " + second)
                .orElse("invalid row");
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private static BulkFailure failure(int index, String reference, String reason) {
        return BulkFailure.builder()
                .index(index)
                .reference(reference)
                .reason(reason)
                .build();
    }

    private Airport requireAirport(Long id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("airport not found with id: " + id));
    }

    private City requireCity(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("city not found with id: " + cityId));
    }
}
