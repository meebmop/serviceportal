package com.example.serviceportal.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRoleUpdateDto {

    @NotBlank(message = "Die Rolle darf nicht leer sein.")
    private String role;

    public UserRoleUpdateDto() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}