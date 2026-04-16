package com.example.serviceportal.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
    private final Map<String, String> fieldErrors;

    public ApiErrorResponse(int status, String message, Map<String, String> fieldErrors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}