package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.UserResponseDto;
import com.example.serviceportal.backend.dto.UserRoleUpdateDto;
import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.UserRole;
import com.example.serviceportal.backend.repository.ServiceRequestRepository;
import com.example.serviceportal.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public UserService(UserRepository userRepository, ServiceRequestRepository serviceRequestRepository) {
        this.userRepository = userRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserResponseDto updateRole(Long id, UserRoleUpdateDto roleUpdateDto, Authentication authentication) {
        UserRole newRole = parseRole(roleUpdateDto.getRole());
        User existingUser = findUserByIdOrThrow(id);
        User currentAdmin = findAuthenticatedUser(authentication);

        if (currentAdmin.getId().equals(existingUser.getId()) && newRole != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Du kannst dir die Administratorrolle nicht selbst entziehen.");
        }

        if (existingUser.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN && isLastAdmin(existingUser)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Der letzte verbleibende Administrator kann nicht herabgestuft werden.");
        }

        existingUser.setRole(newRole);

        User savedUser = userRepository.save(existingUser);

        return mapToDto(savedUser);
    }

    public void deleteUser(Long id, Authentication authentication) {
        User existingUser = findUserByIdOrThrow(id);
        User currentAdmin = findAuthenticatedUser(authentication);

        if (currentAdmin.getId().equals(existingUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Du kannst dein eigenes Benutzerkonto nicht löschen.");
        }

        if (existingUser.getRole() == UserRole.ADMIN) {
            if (isLastAdmin(existingUser)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Der letzte verbleibende Administrator kann nicht gelöscht werden.");
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Administratoren können aus Sicherheitsgründen nicht gelöscht werden.");
        }

        if (serviceRequestRepository.existsByUser_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Benutzer mit vorhandenen Serviceanfragen können nicht gelöscht werden.");
        }

        userRepository.delete(existingUser);
    }

    private User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Benutzer nicht gefunden"));
    }

    private User findAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht angemeldet");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht angemeldet"));
    }

    private boolean isLastAdmin(User user) {
        return user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1;
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.fromValue(role);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ungültige Rolle");
        }
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString());
    }
}