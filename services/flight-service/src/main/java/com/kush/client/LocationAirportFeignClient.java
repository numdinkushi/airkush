package com.kush.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.kush.payload.response.AirportResponse;
import com.kush.payload.response.ApiResponse;

@FeignClient(name = "location-service")
public interface LocationAirportFeignClient {

    @GetMapping("/api/airports/iata/{iataCode}")
    ApiResponse<AirportResponse> getAirportByIata(@PathVariable String iataCode);
}
