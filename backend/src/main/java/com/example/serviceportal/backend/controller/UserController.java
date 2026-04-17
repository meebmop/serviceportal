package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.UserResponseDto;
import com.example.serviceportal.backend.dto.UserRoleUpdateDto;
import com.example.serviceportal.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateDto roleUpdateDto,
            Authentication authentication) {
        return userService.updateRole(id, roleUpdateDto, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id, authentication);
    }
}