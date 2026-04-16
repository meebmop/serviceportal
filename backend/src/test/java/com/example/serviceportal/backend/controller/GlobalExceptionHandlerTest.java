package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_shouldReturnFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Ungültige E-Mail"));
        bindingResult.addError(new FieldError("request", "password", "Passwort zu kurz"));

        Method method = DummyController.class.getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validierungsfehler", response.getBody().getMessage());
        assertEquals("Ungültige E-Mail", response.getBody().getFieldErrors().get("email"));
        assertEquals("Passwort zu kurz", response.getBody().getFieldErrors().get("password"));
    }

    @Test
    void handleResponseStatus_shouldReturnReasonMessage() {
        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Nicht gefunden");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatus(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Nicht gefunden", response.getBody().getMessage());
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        AccessDeniedException ex = new AccessDeniedException("forbidden");

        ResponseEntity<ApiErrorResponse> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Zugriff verweigert", response.getBody().getMessage());
    }

    @Test
    void handleConstraintViolation_shouldReturnMappedViolations() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("createRequest.subject");
        when(violation.getMessage()).thenReturn("darf nicht leer sein");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validierungsfehler", response.getBody().getMessage());
        assertEquals(
                "darf nicht leer sein",
                response.getBody().getFieldErrors().get("createRequest.subject")
        );
    }

    @Test
    void handleUnexpected_shouldReturnInternalServerError() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Ein unerwarteter Fehler ist aufgetreten.", response.getBody().getMessage());
    }

    private static class DummyController {
        @SuppressWarnings("unused")
        public void dummy(String value) {
        }
    }
}