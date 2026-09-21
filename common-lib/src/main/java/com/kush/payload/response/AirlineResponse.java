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
public class AirlineResponse {

    private Long id;
    private String name;
    private String iataCode;
    private String icaoCode;
    private Long ownerUserId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
