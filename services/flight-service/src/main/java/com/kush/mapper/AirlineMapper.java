package com.kush.mapper;

import com.kush.model.Airline;
import com.kush.payload.request.AirlineRequest;
import com.kush.payload.response.AirlineResponse;

public final class AirlineMapper {

    private AirlineMapper() {
    }

    public static Airline toEntity(AirlineRequest request, Long ownerUserId) {
        if (request == null) {
            return null;
        }
        return Airline.builder()
                .name(trim(request.getName()))
                .iataCode(upper(request.getIataCode()))
                .icaoCode(upper(request.getIcaoCode()))
                .ownerUserId(ownerUserId)
                .active(true)
                .build();
    }

    public static AirlineResponse toResponse(Airline airline) {
        if (airline == null) {
            return null;
        }
        return AirlineResponse.builder()
                .id(airline.getId())
                .name(airline.getName())
                .iataCode(airline.getIataCode())
                .icaoCode(airline.getIcaoCode())
                .ownerUserId(airline.getOwnerUserId())
                .active(airline.isActive())
                .createdAt(airline.getCreatedAt())
                .updatedAt(airline.getUpdatedAt())
                .build();
    }

    public static void applyUpdate(Airline airline, AirlineRequest request) {
        if (request == null || airline == null) {
            return;
        }
        if (request.getName() != null) {
            airline.setName(trim(request.getName()));
        }
        if (request.getIataCode() != null) {
            airline.setIataCode(upper(request.getIataCode()));
        }
        if (request.getIcaoCode() != null) {
            airline.setIcaoCode(upper(request.getIcaoCode()));
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String upper(String value) {
        return value == null ? null : value.toUpperCase().trim();
    }
}
