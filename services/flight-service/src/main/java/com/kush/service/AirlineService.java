package com.kush.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kush.payload.request.AirlineRequest;
import com.kush.payload.response.AirlineResponse;

public interface AirlineService {

    AirlineResponse createAirline(AirlineRequest request);

    AirlineResponse getAirlineById(Long id);

    AirlineResponse getAirlineByIataCode(String iataCode);

    AirlineResponse updateAirline(Long id, AirlineRequest request);

    void deleteAirline(Long id);

    Page<AirlineResponse> getAllAirlines(String search, Pageable pageable);

    boolean airlineExists(String iataCode);
}
