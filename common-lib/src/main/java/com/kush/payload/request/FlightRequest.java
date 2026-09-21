package com.kush.payload.request;

import java.time.LocalDateTime;

import com.kush.enums.FlightStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlightRequest {

    @NotNull(message = "Airline id is required")
    private Long airlineId;

    @NotBlank(message = "Flight number is required")
    @Size(max = 8)
    @Pattern(regexp = "^[A-Za-z0-9]{1,8}$", message = "Flight number must be 1-8 letters or digits")
    private String flightNumber;

    @NotBlank(message = "Origin IATA is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Origin IATA must be 3 letters")
    private String originIata;

    @NotBlank(message = "Destination IATA is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Destination IATA must be 3 letters")
    private String destinationIata;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    private FlightStatus status;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
}
