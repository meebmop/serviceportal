package com.example.serviceportal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RequestCreateDto {

    @NotNull(message = "Es muss ein Serviceangebot ausgewählt werden.")
    private Long serviceOfferId;

    @NotBlank(message = "Der Betreff darf nicht leer sein.")
    @Size(min = 3, max = 100, message = "Der Betreff muss zwischen 3 und 100 Zeichen lang sein.")
    private String subject;

    @NotBlank(message = "Die Nachricht darf nicht leer sein.")
    @Size(min = 10, max = 1000, message = "Die Nachricht muss zwischen 10 und 1000 Zeichen lang sein.")
    private String message;

    private String priority;

    public RequestCreateDto() {
    }

    public Long getServiceOfferId() {
        return serviceOfferId;
    }

    public void setServiceOfferId(Long serviceOfferId) {
        this.serviceOfferId = serviceOfferId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}