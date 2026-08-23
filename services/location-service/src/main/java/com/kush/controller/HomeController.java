package com.kush.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kush.payload.response.ApiResponse;

@RestController
public class HomeController {

    @GetMapping()
    public ApiResponse home() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Testing again");
        return apiResponse;
    }
}
