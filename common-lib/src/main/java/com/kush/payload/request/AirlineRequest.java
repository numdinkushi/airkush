package com.kush.payload.request;

import jakarta.validation.constraints.NotBlank;
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
public class AirlineRequest {

    @NotBlank(message = "Airline name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "IATA code is required")
    @Pattern(regexp = "^[A-Za-z0-9]{2}$", message = "IATA code must be 2 letters or digits")
    private String iataCode;

    @Pattern(regexp = "^[A-Za-z]{3}$", message = "ICAO code must be 3 letters")
    private String icaoCode;

    private Long ownerUserId;
}
