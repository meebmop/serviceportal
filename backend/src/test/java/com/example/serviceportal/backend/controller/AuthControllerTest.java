package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.LoginRequestDto;
import com.example.serviceportal.backend.dto.LoginResponseDto;
import com.example.serviceportal.backend.dto.RegisterRequestDto;
import com.example.serviceportal.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_shouldDelegateToService() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setName("Marie");
        dto.setEmail("marie@test.de");
        dto.setPassword("Test123!");

        LoginResponseDto expected = new LoginResponseDto(1L, "Marie", "marie@test.de", "USER");

        when(authService.register(dto)).thenReturn(expected);

        LoginResponseDto result = authController.register(dto);

        assertEquals(1L, result.getId());
        assertEquals("Marie", result.getName());
        assertEquals("USER", result.getRole());
        verify(authService).register(dto);
    }

    @Test
    void login_shouldDelegateToService() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("marie@test.de");
        dto.setPassword("Test123!");

        LoginResponseDto expected = new LoginResponseDto(1L, "Marie", "marie@test.de", "USER");

        when(authService.login(dto, request)).thenReturn(expected);

        LoginResponseDto result = authController.login(dto, request);

        assertEquals("marie@test.de", result.getEmail());
        verify(authService).login(dto, request);
    }

    @Test
    void me_shouldDelegateToService() {
        LoginResponseDto expected = new LoginResponseDto(1L, "Marie", "marie@test.de", "ADMIN");

        when(authService.getCurrentUser(authentication)).thenReturn(expected);

        LoginResponseDto result = authController.me(authentication);

        assertEquals("ADMIN", result.getRole());
        verify(authService).getCurrentUser(authentication);
    }

    @Test
    void logout_shouldDelegateToService() {
        authController.logout(request, response);

        verify(authService).logout(request, response);
    }
}