package com.kush.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.kush.payload.request.AirportRequest;
import com.kush.payload.request.BulkRequest;
import com.kush.payload.response.AirportResponse;
import com.kush.payload.response.ApiResponse;
import com.kush.payload.response.BulkResult;
import com.kush.payload.response.PageResponse;
import com.kush.service.AirportService;
import com.kush.web.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/airports")
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    public ResponseEntity<ApiResponse<AirportResponse>> createAirport(
            @Valid @RequestBody AirportRequest airportRequest
    ) {
        return ApiResponses.created(
                airportService.createAirport(airportRequest),
                "Airport created successfully"
        );
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkResult<AirportResponse>>> createAirports(
            @Valid @RequestBody BulkRequest<AirportRequest> request
    ) {
        return bulkResponse(airportService.createAirports(request.getItems()), "Airports");
    }

    @PostMapping(value = "/bulk/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkResult<AirportResponse>>> importAirports(
            @RequestParam("file") MultipartFile file
    ) {
        return bulkResponse(airportService.importAirports(file), "Airports");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AirportResponse>> getAirportById(@PathVariable Long id) {
        return ApiResponses.ok(airportService.getAirportById(id), "Airport retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AirportResponse>> updateAirport(
            @PathVariable Long id,
            @Valid @RequestBody AirportRequest airportRequest
    ) {
        return ApiResponses.ok(
                airportService.updateAirport(id, airportRequest),
                "Airport updated successfully"
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAirport(@PathVariable Long id) {
        airportService.deleteAirport(id);
        return ApiResponses.ok("Airport deleted successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AirportResponse>>> getAllAirports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponses.ok(
                PageResponse.from(airportService.getAllAirports(search, pageable)),
                "Airports retrieved successfully"
        );
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<PageResponse<AirportResponse>>> getAirportsByCityId(
            @PathVariable Long cityId,
            Pageable pageable
    ) {
        return ApiResponses.ok(
                PageResponse.from(airportService.getAirportsByCityId(cityId, pageable)),
                "Airports retrieved successfully"
        );
    }

    @GetMapping("/iata/{iataCode}")
    public ResponseEntity<ApiResponse<AirportResponse>> getAirportByIataCode(
            @PathVariable String iataCode
    ) {
        return ApiResponses.ok(
                airportService.getAirportByIataCode(iataCode),
                "Airport retrieved successfully"
        );
    }

    @GetMapping("/exists/{iataCode}")
    public ResponseEntity<ApiResponse<Void>> airportExists(@PathVariable String iataCode) {
        return ApiResponses.ok(
                airportService.airportExists(iataCode) ? "Airport exists" : "Airport does not exist"
        );
    }

    private ResponseEntity<ApiResponse<BulkResult<AirportResponse>>> bulkResponse(
            BulkResult<AirportResponse> result,
            String label
    ) {
        String message = label + " bulk import completed: "
                + result.getCreatedCount() + " created, "
                + result.getFailedCount() + " failed";
        if (result.getFailedCount() == 0) {
            return ApiResponses.created(result, message);
        }
        return ApiResponses.ok(result, message);
    }
}
