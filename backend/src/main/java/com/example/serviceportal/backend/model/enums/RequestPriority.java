package com.example.serviceportal.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestPriority {
    NIEDRIG("Niedrig"),
    NORMAL("Normal"),
    HOCH("Hoch");

    private final String value;

    RequestPriority(String value) {
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
    public static RequestPriority fromValue(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL;
        }

        for (RequestPriority priority : values()) {
            if (priority.value.equalsIgnoreCase(value.trim())) {
                return priority;
            }
        }

        throw new IllegalArgumentException("Ungültige Priorität: " + value);
    }
}