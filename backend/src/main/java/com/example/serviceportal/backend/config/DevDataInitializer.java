package com.example.serviceportal.backend.config;

import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.UserRole;
import com.example.serviceportal.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initUsers() {
        createOrUpdateUser("Anna Admin", "admin@serviceportal.de", UserRole.ADMIN, "Admin123!");
        createOrUpdateUser("Max Mustermann", "max@serviceportal.de", UserRole.USER, "User123!");
    }

    private void createOrUpdateUser(String name, String email, UserRole role, String password) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}