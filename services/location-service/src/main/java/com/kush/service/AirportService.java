package com.kush.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.kush.payload.request.AirportRequest;
import com.kush.payload.response.AirportResponse;
import com.kush.payload.response.BulkResult;

public interface AirportService {

    AirportResponse createAirport(AirportRequest request);

    AirportResponse getAirportById(Long id);

    AirportResponse getAirportByIataCode(String iataCode);

    AirportResponse updateAirport(Long id, AirportRequest request);

    void deleteAirport(Long id);

    Page<AirportResponse> getAllAirports(String search, Pageable pageable);

    Page<AirportResponse> getAirportsByCityId(Long cityId, Pageable pageable);

    boolean airportExists(String iataCode);

    BulkResult<AirportResponse> createAirports(List<AirportRequest> requests);

    BulkResult<AirportResponse> importAirports(MultipartFile file);
}
