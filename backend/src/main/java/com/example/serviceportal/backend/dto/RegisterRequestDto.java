package com.example.serviceportal.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequestDto {

    @NotBlank(message = "Der Name darf nicht leer sein.")
    @Size(min = 2, max = 100, message = "Der Name muss zwischen 2 und 100 Zeichen lang sein.")
    private String name;

    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
    @NotBlank(message = "Die E-Mail darf nicht leer sein.")
    @Size(max = 255, message = "Die E-Mail darf maximal 255 Zeichen lang sein.")
    private String email;

    @NotBlank(message = "Das Passwort darf nicht leer sein.")
    @Size(min = 8, max = 100, message = "Das Passwort muss zwischen 8 und 100 Zeichen lang sein.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
            message = "Das Passwort muss Großbuchstaben, Kleinbuchstaben, eine Zahl und ein Sonderzeichen enthalten."
    )
    private String password;

    public RegisterRequestDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
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