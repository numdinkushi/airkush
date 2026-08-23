package com.kush.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kush.payload.request.CityRequest;
import com.kush.payload.response.CityResponse;

public interface CityService {

    CityResponse createCity(CityRequest request);

    CityResponse getCityById(Long id);

    CityResponse updateCity(Long id, CityRequest request);

    void deleteCity(Long id);

    Page<CityResponse> getAllCities(Pageable pageable);

    Page<CityResponse> searchCities(String keyword, Pageable pageable);

    Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable);

    boolean cityExists(String cityCode);

}
