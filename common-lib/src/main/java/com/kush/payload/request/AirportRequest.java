package com.kush.payload.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class AirportRequest {

    @NotBlank(message = "Airport name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "IATA code is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "IATA code must be 3 letters")
    private String iataCode;

    @NotBlank(message = "ICAO code is required")
    @Pattern(regexp = "^[A-Za-z]{4}$", message = "ICAO code must be 4 letters")
    private String icaoCode;

    @NotNull(message = "City id is required")
    private Long cityId;

    @Size(max = 50)
    private String timeZoneId;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
}
