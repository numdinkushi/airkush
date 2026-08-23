package com.kush.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kush.mapper.CityMapper;
import com.kush.model.City;
import com.kush.payload.request.CityRequest;
import com.kush.payload.response.CityResponse;
import com.kush.repository.CityRepository;
import com.kush.service.CityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public CityResponse createCity(CityRequest request) {
        if (cityRepository.existsByCityCode(request.getCityCode())) {
            throw new IllegalArgumentException("city with given code already exist");
        }

        City city = CityMapper.toEntity(request);
        City updatedCity = cityRepository.save(city);
        return CityMapper.toResponse(updatedCity);
    }

    @Override
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("city not found with id: " + id));
        return CityMapper.toResponse(city);
    }

    @Override
    public CityResponse updateCity(Long id, CityRequest request) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("city not found with id: " + id));

        if (request.getCityCode() != null
                && cityRepository.existsByCityCodeAndIdNot(request.getCityCode(), id)) {
            throw new IllegalArgumentException("city with given code already exist");
        }

        CityMapper.updateEntity(city, request);
        City savedCity = cityRepository.save(city);
        return CityMapper.toResponse(savedCity);
    }

    @Override
    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new IllegalArgumentException("city not found with id: " + id);
        }
        cityRepository.deleteById(id);
    }

    @Override
    public Page<CityResponse> getAllCities(Pageable pageable) {
        return cityRepository.findAll(pageable).map(CityMapper::toResponse);
    }

    @Override
    public Page<CityResponse> searchCities(String keyword, Pageable pageable) {
        return cityRepository.searchByKeyword(keyword, pageable).map(CityMapper::toResponse);
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
}
