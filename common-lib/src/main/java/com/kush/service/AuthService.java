package com.kush.service;

import com.kush.payload.dto.UserDTO;
import com.kush.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(String email, String password);

    AuthResponse signup(UserDTO req);
}
