package com.kush.mapper;

import org.springframework.util.StringUtils;

import com.kush.enums.UserRole;
import com.kush.model.User;
import com.kush.payload.dto.UserDTO;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserDTO request, String encodedPassword, UserRole role) {
        return User.builder()
                .fullName(trim(request.getFullName()))
                .email(normalizeEmail(request.getEmail()))
                .password(encodedPassword)
                .phone(trim(request.getPhone()))
                .role(role)
                .build();
    }

    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .build();
    }

    public static void applyUpdate(User user, UserDTO request) {
        if (request.getFullName() != null) {
            user.setFullName(trim(request.getFullName()));
        }
        if (request.getEmail() != null) {
            user.setEmail(normalizeEmail(request.getEmail()));
        }
        if (request.getPhone() != null) {
            user.setPhone(trim(request.getPhone()));
        }
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
