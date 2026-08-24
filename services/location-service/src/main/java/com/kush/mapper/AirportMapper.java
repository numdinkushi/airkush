package com.kush.mapper;

import com.kush.model.Airport;
import com.kush.model.City;
import com.kush.payload.request.AirportRequest;
import com.kush.payload.response.AirportResponse;

public class AirportMapper {

    public static Airport toEntity(AirportRequest request, City city) {
        if (request == null) return null;
        return Airport.builder()
                .name(trim(request.getName()))
                .iataCode(upper(request.getIataCode()))
                .icaoCode(upper(request.getIcaoCode()))
                .city(city)
                .timeZoneId(trim(request.getTimeZoneId()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    public static AirportResponse toResponse(Airport airport) {
        if (airport == null) return null;
        City city = airport.getCity();
        return AirportResponse.builder()
                .id(airport.getId())
                .name(airport.getName())
                .iataCode(airport.getIataCode())
                .icaoCode(airport.getIcaoCode())
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getName() : null)
                .cityCode(city != null ? city.getCityCode() : null)
                .countryCode(city != null ? city.getCountryCode() : null)
                .countryName(city != null ? city.getCountryName() : null)
                .timeZoneId(airport.getTimeZoneId())
                .latitude(airport.getLatitude())
                .longitude(airport.getLongitude())
                .createdAt(airport.getCreatedAt())
                .updatedAt(airport.getUpdatedAt())
                .build();
    }

    public static Airport updateEntity(Airport airport, AirportRequest request, City city) {
        if (request == null || airport == null) return airport;

        if (request.getName() != null) {
            airport.setName(request.getName().trim());
        }
        if (request.getIataCode() != null) {
            airport.setIataCode(request.getIataCode().toUpperCase().trim());
        }
        if (request.getIcaoCode() != null) {
            airport.setIcaoCode(request.getIcaoCode().toUpperCase().trim());
        }
        if (city != null) {
            airport.setCity(city);
        }
        if (request.getTimeZoneId() != null) {
            airport.setTimeZoneId(request.getTimeZoneId().trim());
        }
        if (request.getLatitude() != null) {
            airport.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            airport.setLongitude(request.getLongitude());
        }

        return airport;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String upper(String value) {
        return value == null ? null : value.toUpperCase().trim();
    }
}
