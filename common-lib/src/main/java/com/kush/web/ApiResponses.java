package com.kush.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kush.payload.response.ApiResponse;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data, message));
    }

    public static ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
