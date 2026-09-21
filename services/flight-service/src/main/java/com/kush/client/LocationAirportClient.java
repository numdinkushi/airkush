package com.kush.client;

import org.springframework.stereotype.Component;

import com.kush.exception.BadRequestException;
import com.kush.exception.ResourceNotFoundException;
import com.kush.payload.response.AirportResponse;
import com.kush.payload.response.ApiResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocationAirportClient {

    private final LocationAirportFeignClient feignClient;

    public AirportResponse requireByIata(String iataCode) {
        String code = iataCode.toUpperCase().trim();
        try {
            ApiResponse<AirportResponse> response = feignClient.getAirportByIata(code);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("airport not found with IATA code: " + code);
            }
            return response.getData();
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("airport not found with IATA code: " + code);
        } catch (FeignException ex) {
            throw new BadRequestException("Unable to verify airport " + code + ": " + ex.getMessage());
        }
    }
}
