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
import com.kush.csv.LocationCsvMapper.Indexed;
import com.kush.exception.ConflictException;
import com.kush.exception.ResourceNotFoundException;
import com.kush.mapper.CityMapper;
import com.kush.model.City;
import com.kush.payload.request.CityRequest;
import com.kush.payload.response.BulkFailure;
import com.kush.payload.response.BulkResult;
import com.kush.payload.response.CityResponse;
import com.kush.repository.AirportRepository;
import com.kush.repository.CityRepository;
import com.kush.service.CityService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final AirportRepository airportRepository;
    private final LocationCsvMapper csvMapper;
    private final Validator validator;

    @Override
    public CityResponse createCity(CityRequest request) {
        if (cityRepository.existsByCityCode(request.getCityCode())) {
            throw new ConflictException("city with given code already exist");
        }

        City city = CityMapper.toEntity(request);
        City updatedCity = cityRepository.save(city);
        return CityMapper.toResponse(updatedCity);
    }

    @Override
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("city not found with id: " + id));
        return CityMapper.toResponse(city);
    }

    @Override
    public CityResponse updateCity(Long id, CityRequest request) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("city not found with id: " + id));

        if (request.getCityCode() != null
                && cityRepository.existsByCityCodeAndIdNot(request.getCityCode(), id)) {
            throw new ConflictException("city with given code already exist");
        }

        CityMapper.updateEntity(city, request);
        City savedCity = cityRepository.save(city);
        return CityMapper.toResponse(savedCity);
    }

    @Override
    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new ResourceNotFoundException("city not found with id: " + id);
        }
        if (airportRepository.existsByCityId(id)) {
            throw new ConflictException("cannot delete city with existing airports");
        }
        cityRepository.deleteById(id);
    }

    @Override
    public Page<CityResponse> getAllCities(String search, Pageable pageable) {
        Page<City> cities = StringUtils.hasText(search)
                ? cityRepository.searchByKeyword(search.trim(), pageable)
                : cityRepository.findAll(pageable);
        return cities.map(CityMapper::toResponse);
    }

    @Override
    public Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable) {
        return cityRepository.findByCountryCodeIgnoreCase(countryCode, pageable)
                .map(CityMapper::toResponse);
    }

    @Override
    public boolean cityExists(String cityCode) {
        return cityRepository.existsByCityCode(cityCode);
    }

    @Override
    public BulkResult<CityResponse> createCities(List<CityRequest> requests) {
        List<CityResponse> created = new ArrayList<>();
        List<BulkFailure> failed = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        for (int i = 0; i < requests.size(); i++) {
            CityRequest request = requests.get(i);
            String code = normalize(request.getCityCode());
            int index = i + 1;
            if (code != null && !seenCodes.add(code)) {
                failed.add(failure(index, code, "duplicate city code in this request"));
                continue;
            }
            saveCity(index, code, request, created, failed);
        }
        return BulkResult.of(created, failed, requests.size());
    }

    @Override
    public BulkResult<CityResponse> importCities(MultipartFile file) {
        List<Indexed<CityRequest>> rows = csvMapper.toCityRequests(file);
        List<CityResponse> created = new ArrayList<>();
        List<BulkFailure> failed = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        for (Indexed<CityRequest> row : rows) {
            CityRequest request = row.value();
            String code = normalize(request.getCityCode());
            String violations = violationsOf(request);
            if (violations != null) {
                failed.add(failure(row.index(), code, violations));
                continue;
            }
            if (code != null && !seenCodes.add(code)) {
                failed.add(failure(row.index(), code, "duplicate city code in this file"));
                continue;
            }
            saveCity(row.index(), code, request, created, failed);
        }
        return BulkResult.of(created, failed, rows.size());
    }

    private void saveCity(
            int index,
            String code,
            CityRequest request,
            List<CityResponse> created,
            List<BulkFailure> failed
    ) {
        try {
            created.add(createCity(request));
        } catch (ConflictException | ResourceNotFoundException ex) {
            failed.add(failure(index, code, ex.getMessage()));
        }
    }

    private String violationsOf(CityRequest request) {
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
}
