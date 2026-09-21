package com.kush.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kush.enums.FlightStatus;
import com.kush.payload.request.FlightRequest;
import com.kush.payload.response.FlightResponse;

public interface FlightService {

    FlightResponse createFlight(FlightRequest request);

    FlightResponse getFlightById(Long id);

    FlightResponse updateFlight(Long id, FlightRequest request);

    void deleteFlight(Long id);

    Page<FlightResponse> searchFlights(
            String origin,
            String destination,
            LocalDate date,
            Long airlineId,
            FlightStatus status,
            String search,
            Pageable pageable
    );

    Page<FlightResponse> getFlightsByAirline(Long airlineId, Pageable pageable);
}
