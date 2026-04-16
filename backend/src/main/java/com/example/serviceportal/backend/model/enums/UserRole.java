package com.example.serviceportal.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value.trim())) {
                return role;
            }
        }

        throw new IllegalArgumentException("Ungültige Rolle: " + value);
    }
}