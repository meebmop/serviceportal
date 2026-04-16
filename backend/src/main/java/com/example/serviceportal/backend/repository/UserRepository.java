package com.example.serviceportal.backend.repository;

import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    long countByRole(UserRole role);
}