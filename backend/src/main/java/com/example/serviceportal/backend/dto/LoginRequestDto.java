package com.example.serviceportal.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDto {

    @NotBlank(message = "Die E-Mail darf nicht leer sein.")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
    @Size(max = 255, message = "Die E-Mail darf maximal 255 Zeichen lang sein.")
    private String email;

    @NotBlank(message = "Das Passwort darf nicht leer sein.")
    @Size(max = 100, message = "Das Passwort darf maximal 100 Zeichen lang sein.")
    private String password;

    public LoginRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}