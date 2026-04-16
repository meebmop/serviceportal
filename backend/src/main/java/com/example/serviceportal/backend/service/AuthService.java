package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.LoginRequestDto;
import com.example.serviceportal.backend.dto.LoginResponseDto;
import com.example.serviceportal.backend.dto.RegisterRequestDto;
import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.serviceportal.backend.model.enums.UserRole;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LoginAttemptService loginAttemptService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    public LoginResponseDto register(RegisterRequestDto registerRequest) {
        String normalizedEmail = normalizeEmail(registerRequest.getEmail());

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Für diese E-Mail-Adresse existiert bereits ein Benutzer."
            );
        }

        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(normalizedEmail);
        user.setRole(UserRole.USER);
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));

        User savedUser = userRepository.save(user);
        return mapToLoginResponse(savedUser);
    }

    public LoginResponseDto login(LoginRequestDto loginRequest, HttpServletRequest request) {
        String normalizedEmail = normalizeEmail(loginRequest.getEmail());

        if (loginAttemptService.isLocked(normalizedEmail)) {
            long remainingSeconds = loginAttemptService.getRemainingLockSeconds(normalizedEmail);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    buildLockedMessage(remainingSeconds)
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, loginRequest.getPassword())
            );

            storeAuthenticationInSession(authentication, request);
            loginAttemptService.loginSucceeded(normalizedEmail);

            User user = findUserByEmailOrUnauthorized(normalizedEmail);
            return mapToLoginResponse(user);
        } catch (BadCredentialsException ex) {
            int attempts = loginAttemptService.loginFailed(normalizedEmail);

            if (attempts >= loginAttemptService.getMaxAttempts()) {
                long remainingSeconds = loginAttemptService.getRemainingLockSeconds(normalizedEmail);

                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        buildLockedMessage(remainingSeconds)
                );
            }

            int remainingAttempts = loginAttemptService.getRemainingAttempts(normalizedEmail);

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    buildInvalidCredentialsMessage(remainingAttempts)
            );
        }
    }

    public LoginResponseDto getCurrentUser(Authentication authentication) {
        String userEmail = extractAuthenticatedEmail(authentication);
        User user = findUserByEmailOrUnauthorized(userEmail);
        return mapToLoginResponse(user);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void storeAuthenticationInSession(Authentication authentication, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }

    private String extractAuthenticatedEmail(Authentication authentication) {
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht angemeldet");
        }

        return authentication.getName();
    }

    private User findUserByEmailOrUnauthorized(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht angemeldet"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String buildInvalidCredentialsMessage(int remainingAttempts) {
        if (remainingAttempts <= 1) {
            return "Ungültige E-Mail oder ungültiges Passwort. Noch 1 Versuch bis zur vorübergehenden Sperre.";
        }

        return "Ungültige E-Mail oder ungültiges Passwort. Noch "
                + remainingAttempts
                + " Versuche bis zur vorübergehenden Sperre.";
    }

    private String buildLockedMessage(long remainingSeconds) {
        if (remainingSeconds >= 60) {
            long minutes = (long) Math.ceil(remainingSeconds / 60.0);
            return "Zu viele fehlgeschlagene Anmeldeversuche. Bitte versuche es in "
                    + minutes
                    + " Minute(n) erneut.";
        }

        return "Zu viele fehlgeschlagene Anmeldeversuche. Bitte versuche es in "
                + remainingSeconds
                + " Sekunde(n) erneut.";
    }

    private LoginResponseDto mapToLoginResponse(User user) {
        return new LoginResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString()
        );
    }
}