package com.kush.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.kush.payload.request.AirlineRequest;
import com.kush.payload.response.AirlineResponse;
import com.kush.payload.response.ApiResponse;
import com.kush.payload.response.PageResponse;
import com.kush.service.AirlineService;
import com.kush.web.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/airlines")
public class AirlineController {

    private final AirlineService airlineService;

    @PostMapping
    public ResponseEntity<ApiResponse<AirlineResponse>> createAirline(
            @Valid @RequestBody AirlineRequest request
    ) {
        return ApiResponses.created(airlineService.createAirline(request), "Airline created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AirlineResponse>> getAirlineById(@PathVariable Long id) {
        return ApiResponses.ok(airlineService.getAirlineById(id), "Airline retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AirlineResponse>> updateAirline(
            @PathVariable Long id,
            @Valid @RequestBody AirlineRequest request
    ) {
        return ApiResponses.ok(airlineService.updateAirline(id, request), "Airline updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAirline(@PathVariable Long id) {
        airlineService.deleteAirline(id);
        return ApiResponses.ok("Airline deleted successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AirlineResponse>>> getAllAirlines(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponses.ok(
                PageResponse.from(airlineService.getAllAirlines(search, pageable)),
                "Airlines retrieved successfully"
        );
    }

    @GetMapping("/code/{iataCode}")
    public ResponseEntity<ApiResponse<AirlineResponse>> getAirlineByIataCode(
            @PathVariable String iataCode
    ) {
        return ApiResponses.ok(
                airlineService.getAirlineByIataCode(iataCode),
                "Airline retrieved successfully"
        );
    }

    @GetMapping("/exists/{iataCode}")
    public ResponseEntity<ApiResponse<Void>> airlineExists(@PathVariable String iataCode) {
        return ApiResponses.ok(
                airlineService.airlineExists(iataCode) ? "Airline exists" : "Airline does not exist"
        );
    }
}
