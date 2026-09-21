package com.kush.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kush.payload.response.ApiResponse;
import com.kush.web.ApiResponses;

@RestController
public class HomeController {

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> home() {
        return ApiResponses.ok("Flight service is running");
    }
}
