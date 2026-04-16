package com.example.serviceportal.backend.model;

import com.example.serviceportal.backend.model.enums.UserRole;
import com.example.serviceportal.backend.persistence.UserRoleConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "app_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_user_email", columnNames = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Der Name darf nicht leer sein.")
    @Size(min = 2, max = 100, message = "Der Name muss zwischen 2 und 100 Zeichen lang sein.")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Die E-Mail darf nicht leer sein.")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
    @Size(max = 255, message = "Die E-Mail darf maximal 255 Zeichen lang sein.")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Convert(converter = UserRoleConverter.class)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @NotBlank(message = "Das Passwort darf nicht leer sein.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    public User() {
    }

    public User(String name, String email, UserRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}