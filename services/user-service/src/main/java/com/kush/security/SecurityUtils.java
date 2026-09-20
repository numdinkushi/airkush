package com.kush.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kush.enums.UserRole;
import com.kush.exception.UnauthorizedException;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<String> currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }

    public static String requireEmail() {
        return currentEmail().orElseThrow(() -> new UnauthorizedException("Authentication required"));
    }

    public static boolean hasRole(UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String authority = role.authority();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }
}
