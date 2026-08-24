package com.kush.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.kush.payload.request.CityRequest;
import com.kush.payload.response.BulkResult;
import com.kush.payload.response.CityResponse;

public interface CityService {

    CityResponse createCity(CityRequest request);

    CityResponse getCityById(Long id);

    CityResponse updateCity(Long id, CityRequest request);

    void deleteCity(Long id);

    Page<CityResponse> getAllCities(String search, Pageable pageable);

    Page<CityResponse> getCitiesByCountryCode(String countryCode, Pageable pageable);

    boolean cityExists(String cityCode);

    BulkResult<CityResponse> createCities(List<CityRequest> requests);

    BulkResult<CityResponse> importCities(MultipartFile file);

}
