package com.kush.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kush.payload.dto.UserDTO;
import com.kush.payload.response.ApiResponse;
import com.kush.payload.response.PageResponse;
import com.kush.service.UserService;
import com.kush.web.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO request) {
        return ApiResponses.created(userService.createUser(request), "User created successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        return ApiResponses.ok(userService.getUserById(id), "User retrieved successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO request
    ) {
        return ApiResponses.ok(userService.updateUser(id, request), "User updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponses.ok("User deleted successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ApiResponses.ok(
                PageResponse.from(userService.getAllUsers(search, pageable)),
                "Users retrieved successfully"
        );
    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<ApiResponse<Void>> userExists(@PathVariable String email) {
        return ApiResponses.ok(
                userService.userExists(email) ? "User exists" : "User does not exist"
        );
    }
}
