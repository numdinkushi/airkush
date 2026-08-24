package com.kush.payload.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AirportResponse {

    private Long id;
    private String name;
    private String iataCode;
    private String icaoCode;
    private Long cityId;
    private String cityName;
    private String cityCode;
    private String countryCode;
    private String countryName;
    private String timeZoneId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
