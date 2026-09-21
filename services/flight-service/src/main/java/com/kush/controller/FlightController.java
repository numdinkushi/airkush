package com.kush.controller;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kush.enums.FlightStatus;
import com.kush.payload.request.FlightRequest;
import com.kush.payload.response.ApiResponse;
import com.kush.payload.response.FlightResponse;
import com.kush.payload.response.PageResponse;
import com.kush.service.FlightService;
import com.kush.web.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight(
            @Valid @RequestBody FlightRequest request
    ) {
        return ApiResponses.created(flightService.createFlight(request), "Flight created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(@PathVariable Long id) {
        return ApiResponses.ok(flightService.getFlightById(id), "Flight retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody FlightRequest request
    ) {
        return ApiResponses.ok(flightService.updateFlight(id, request), "Flight updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ApiResponses.ok("Flight deleted successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> searchFlights(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long airlineId,
            @RequestParam(required = false) FlightStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponses.ok(
                PageResponse.from(flightService.searchFlights(
                        origin, destination, date, airlineId, status, search, pageable)),
                "Flights retrieved successfully"
        );
    }

    @GetMapping("/airline/{airlineId}")
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> getFlightsByAirline(
            @PathVariable Long airlineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponses.ok(
                PageResponse.from(flightService.getFlightsByAirline(airlineId, pageable)),
                "Flights retrieved successfully"
        );
    }
}
