package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.UserResponseDto;
import com.example.serviceportal.backend.dto.UserRoleUpdateDto;
import com.example.serviceportal.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    @Test
    void getAllUsers_shouldDelegateToService() {
        List<UserResponseDto> expected = List.of(
                new UserResponseDto(1L, "Admin", "admin@test.de", "ADMIN"),
                new UserResponseDto(2L, "User", "user@test.de", "USER"));

        when(userService.getAllUsers()).thenReturn(expected);

        List<UserResponseDto> result = userController.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getRole());
        verify(userService).getAllUsers();
    }

    @Test
    void updateRole_shouldDelegateToService() {
        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setRole("ADMIN");

        UserResponseDto expected = new UserResponseDto(2L, "User", "user@test.de", "ADMIN");

        when(userService.updateRole(2L, dto, authentication)).thenReturn(expected);

        UserResponseDto result = userController.updateRole(2L, dto, authentication);

        assertEquals("ADMIN", result.getRole());
        verify(userService).updateRole(2L, dto, authentication);
    }

    @Test
    void deleteUser_shouldDelegateToService() {
        userController.deleteUser(2L, authentication);

        verify(userService).deleteUser(2L, authentication);
    }
}