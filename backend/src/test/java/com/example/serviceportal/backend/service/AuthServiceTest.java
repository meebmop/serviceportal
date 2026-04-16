package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.LoginRequestDto;
import com.example.serviceportal.backend.dto.LoginResponseDto;
import com.example.serviceportal.backend.dto.RegisterRequestDto;
import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.UserRole;
import com.example.serviceportal.backend.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateNewUser() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setName("Marie");
        dto.setEmail("Marie@Test.de");
        dto.setPassword("Test123!");

        when(userRepository.findByEmailIgnoreCase("marie@test.de")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Test123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setUserId(user, 1L);
            return user;
        });

        LoginResponseDto result = authService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("Marie", savedUser.getName());
        assertEquals("marie@test.de", savedUser.getEmail());
        assertEquals(UserRole.USER, savedUser.getRole());
        assertEquals("hashedPassword", savedUser.getPasswordHash());

        assertEquals(1L, result.getId());
        assertEquals("Marie", result.getName());
        assertEquals("marie@test.de", result.getEmail());
        assertEquals("USER", result.getRole());
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setName("Marie");
        dto.setEmail("marie@test.de");
        dto.setPassword("Test123!");

        when(userRepository.findByEmailIgnoreCase("marie@test.de"))
                .thenReturn(Optional.of(new User()));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(dto)
        );

        assertEquals(HttpStatus.CONFLICT.value(), ex.getStatusCode().value());
        assertEquals("Für diese E-Mail-Adresse existiert bereits ein Benutzer.", ex.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldAuthenticateAndStoreContextInSession() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("Marie@Test.de");
        dto.setPassword("Test123!");

        User user = createUser(1L, "Marie", "marie@test.de", UserRole.USER);

        Authentication authResult = mock(Authentication.class);

        when(loginAttemptService.isLocked("marie@test.de")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authResult);
        when(request.getSession(true)).thenReturn(session);
        when(userRepository.findByEmailIgnoreCase("marie@test.de")).thenReturn(Optional.of(user));

        LoginResponseDto result = authService.login(dto, request);

        verify(loginAttemptService).loginSucceeded("marie@test.de");
        verify(session).setAttribute(
                eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY),
                any(SecurityContext.class)
        );

        assertEquals("Marie", result.getName());
        assertEquals("marie@test.de", result.getEmail());
        assertEquals("USER", result.getRole());
    }

    @Test
    void login_shouldRejectWhenUserIsLocked() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("marie@test.de");
        dto.setPassword("wrong");

        when(loginAttemptService.isLocked("marie@test.de")).thenReturn(true);
        when(loginAttemptService.getRemainingLockSeconds("marie@test.de")).thenReturn(120L);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(dto, request)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("2 Minute(n)"));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_shouldReturnUnauthorizedBeforeLockLimit() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("marie@test.de");
        dto.setPassword("wrong");

        when(loginAttemptService.isLocked("marie@test.de")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));
        when(loginAttemptService.loginFailed("marie@test.de")).thenReturn(2);
        when(loginAttemptService.getMaxAttempts()).thenReturn(5);
        when(loginAttemptService.getRemainingAttempts("marie@test.de")).thenReturn(3);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(dto, request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED.value(), ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Noch 3 Versuche"));
    }

    @Test
    void login_shouldLockAfterLastFailedAttempt() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("marie@test.de");
        dto.setPassword("wrong");

        when(loginAttemptService.isLocked("marie@test.de")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));
        when(loginAttemptService.loginFailed("marie@test.de")).thenReturn(5);
        when(loginAttemptService.getMaxAttempts()).thenReturn(5);
        when(loginAttemptService.getRemainingLockSeconds("marie@test.de")).thenReturn(45L);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(dto, request)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("45 Sekunde(n)"));
    }

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser() {
        User user = createUser(1L, "Marie", "marie@test.de", UserRole.ADMIN);

        when(authentication.getName()).thenReturn("marie@test.de");
        when(userRepository.findByEmailIgnoreCase("marie@test.de")).thenReturn(Optional.of(user));

        LoginResponseDto result = authService.getCurrentUser(authentication);

        assertEquals(1L, result.getId());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void getCurrentUser_shouldRejectAnonymousUser() {
        when(authentication.getName()).thenReturn("anonymousUser");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.getCurrentUser(authentication)
        );

        assertEquals(HttpStatus.UNAUTHORIZED.value(), ex.getStatusCode().value());
        assertEquals("Nicht angemeldet", ex.getReason());
    }

    @Test
    void logout_shouldInvalidateSessionAndClearCookie() {
        when(request.getSession(false)).thenReturn(session);

        authService.logout(request, response);

        verify(session).invalidate();

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());

        Cookie cookie = captor.getValue();
        assertEquals("JSESSIONID", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
    }

    private User createUser(Long id, String name, String email, UserRole role) {
        User user = new User();
        setUserId(user, id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash("hashed");
        return user;
    }

    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}