package com.kush.enums;

public enum UserRole {
    ADMIN,
    OWNER,
    USER;

    public String authority() {
        return "ROLE_" + name();
    }
}
