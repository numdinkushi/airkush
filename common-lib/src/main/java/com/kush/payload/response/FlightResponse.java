package com.kush.payload.response;

import java.time.LocalDateTime;

import com.kush.enums.FlightStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlightResponse {

    private Long id;
    private Long airlineId;
    private String airlineName;
    private String airlineIata;
    private String flightNumber;
    private String originIata;
    private String originName;
    private String destinationIata;
    private String destinationName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private FlightStatus status;
    private Integer totalSeats;
    private Integer availableSeats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
