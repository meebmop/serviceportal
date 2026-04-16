package com.example.serviceportal.backend.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestStatus {
    EINGEGANGEN("Eingegangen"),
    IN_BEARBEITUNG("In Bearbeitung"),
    ABGESCHLOSSEN("Abgeschlossen");

    private final String value;

    RequestStatus(String value) {
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
    public static RequestStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (RequestStatus status : values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Ungültiger Status: " + value);
    }
}