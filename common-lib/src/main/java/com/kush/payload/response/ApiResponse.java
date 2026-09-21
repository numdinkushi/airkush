package com.kush.payload.response;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "message", "data", "errors", "timestamp" })
public class ApiResponse<T> {

    private final boolean success;
    private final String message;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private final T data;

    private final Map<String, String> errors;
    private final Instant timestamp;

    @JsonCreator
    private ApiResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("message") String message,
            @JsonProperty("data") T data,
            @JsonProperty("errors") Map<String, String> errors
    ) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(null, message);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public static <T> ApiResponse<T> failure(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
