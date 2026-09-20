package com.kush.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kush.enums.UserRole;
import com.kush.exception.BadRequestException;
import com.kush.exception.ConflictException;
import com.kush.exception.UnauthorizedException;
import com.kush.mapper.UserMapper;
import com.kush.model.User;
import com.kush.payload.dto.UserDTO;
import com.kush.payload.response.AuthResponse;
import com.kush.repository.UserRepository;
import com.kush.security.JwtService;
import com.kush.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(UserMapper.normalizeEmail(email))
                .filter(found -> passwordEncoder.matches(password, found.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return tokenResponse(user, "Login successful", "Welcome back");
    }

    @Override
    public AuthResponse signup(UserDTO req) {
        String email = UserMapper.normalizeEmail(req.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("user with given email already exist");
        }
        if (req.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("You cannot sign up system admins");
        }

        UserRole role = req.getRole() != null ? req.getRole() : UserRole.USER;
        User saved = userRepository.save(
                UserMapper.toEntity(req, passwordEncoder.encode(req.getPassword()), role)
        );
        return tokenResponse(saved, "Account created successfully", "Welcome");
    }

    private AuthResponse tokenResponse(User user, String message, String title) {
        return AuthResponse.builder()
                .jwt(jwtService.generateToken(user))
                .message(message)
                .title(title)
                .user(UserMapper.toDto(user))
                .build();
    }
}
