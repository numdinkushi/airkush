package com.kush.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kush.payload.dto.UserDTO;
import com.kush.payload.request.LoginRequest;
import com.kush.payload.response.ApiResponse;
import com.kush.payload.response.AuthResponse;
import com.kush.service.AuthService;
import com.kush.service.UserService;
import com.kush.web.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody UserDTO request) {
        return ApiResponses.created(authService.signup(request), "Account created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponses.ok(authService.login(request.getEmail(), request.getPassword()), "Login successful");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> me() {
        return ApiResponses.ok(userService.getCurrentUser(), "Current user retrieved successfully");
    }
}
