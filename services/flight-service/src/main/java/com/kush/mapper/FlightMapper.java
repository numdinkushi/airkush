package com.kush.mapper;

import com.kush.enums.FlightStatus;
import com.kush.model.Airline;
import com.kush.model.Flight;
import com.kush.payload.request.FlightRequest;
import com.kush.payload.response.AirportResponse;
import com.kush.payload.response.FlightResponse;

public final class FlightMapper {

    private FlightMapper() {
    }

    public static Flight toEntity(
            FlightRequest request,
            Airline airline,
            AirportResponse origin,
            AirportResponse destination
    ) {
        int seats = request.getTotalSeats();
        return Flight.builder()
                .airline(airline)
                .flightNumber(upper(request.getFlightNumber()))
                .originIata(origin.getIataCode())
                .originName(origin.getName())
                .destinationIata(destination.getIataCode())
                .destinationName(destination.getName())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .status(request.getStatus() != null ? request.getStatus() : FlightStatus.SCHEDULED)
                .totalSeats(seats)
                .availableSeats(seats)
                .build();
    }

    public static FlightResponse toResponse(Flight flight) {
        if (flight == null) {
            return null;
        }
        Airline airline = flight.getAirline();
        return FlightResponse.builder()
                .id(flight.getId())
                .airlineId(airline != null ? airline.getId() : null)
                .airlineName(airline != null ? airline.getName() : null)
                .airlineIata(airline != null ? airline.getIataCode() : null)
                .flightNumber(flight.getFlightNumber())
                .originIata(flight.getOriginIata())
                .originName(flight.getOriginName())
                .destinationIata(flight.getDestinationIata())
                .destinationName(flight.getDestinationName())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .status(flight.getStatus())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .build();
    }

    public static String upper(String value) {
        return value == null ? null : value.toUpperCase().trim();
    }
}
