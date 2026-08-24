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

import com.kush.payload.request.BulkRequest;
import com.kush.payload.request.CityRequest;
import com.kush.payload.response.ApiResponse;
import com.kush.payload.response.BulkResult;
import com.kush.payload.response.CityResponse;
import com.kush.payload.response.PageResponse;
import com.kush.service.CityService;
import com.kush.web.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    @PostMapping
    public ResponseEntity<ApiResponse<CityResponse>> createCity(
            @Valid @RequestBody CityRequest cityRequest
    ) {
        return ApiResponses.created(cityService.createCity(cityRequest), "City created successfully");
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkResult<CityResponse>>> createCities(
            @Valid @RequestBody BulkRequest<CityRequest> request
    ) {
        return bulkResponse(cityService.createCities(request.getItems()), "Cities");
    }

    @PostMapping(value = "/bulk/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkResult<CityResponse>>> importCities(
            @RequestParam("file") MultipartFile file
    ) {
        return bulkResponse(cityService.importCities(file), "Cities");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CityResponse>> getCityById(@PathVariable Long id) {
        return ApiResponses.ok(cityService.getCityById(id), "City retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CityResponse>> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequest cityRequest
    ) {
        return ApiResponses.ok(cityService.updateCity(id, cityRequest), "City updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ApiResponses.ok("City deleted successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CityResponse>>> getAllCities(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponses.ok(
                PageResponse.from(cityService.getAllCities(search, pageable)),
                "Cities retrieved successfully"
        );
    }

    @GetMapping("/country/{countryCode}")
    public ResponseEntity<ApiResponse<PageResponse<CityResponse>>> getCitiesByCountryCode(
            @PathVariable String countryCode,
            Pageable pageable
    ) {
        return ApiResponses.ok(
                PageResponse.from(cityService.getCitiesByCountryCode(countryCode, pageable)),
                "Cities retrieved successfully"
        );
    }

    @GetMapping("/exists/{cityCode}")
    public ResponseEntity<ApiResponse<Void>> cityExists(@PathVariable String cityCode) {
        return ApiResponses.ok(
                cityService.cityExists(cityCode) ? "City exists" : "City does not exist"
        );
    }

    private ResponseEntity<ApiResponse<BulkResult<CityResponse>>> bulkResponse(
            BulkResult<CityResponse> result,
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
