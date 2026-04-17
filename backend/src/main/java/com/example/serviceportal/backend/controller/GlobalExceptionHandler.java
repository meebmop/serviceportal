package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
                Map<String, String> fieldErrors = new LinkedHashMap<>();

                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        fieldErrors.put(error.getField(), error.getDefaultMessage());
                }

                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Validierungsfehler",
                                                fieldErrors));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
                String message = ex.getReason() != null ? ex.getReason() : "Fehler";

                return ResponseEntity.status(ex.getStatusCode()).body(
                                new ApiErrorResponse(
                                                ex.getStatusCode().value(),
                                                message,
                                                Map.of()));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                new ApiErrorResponse(
                                                HttpStatus.FORBIDDEN.value(),
                                                "Zugriff verweigert",
                                                Map.of()));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
                Map<String, String> fieldErrors = new LinkedHashMap<>();

                for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                        String path = violation.getPropertyPath() != null
                                        ? violation.getPropertyPath().toString()
                                        : "constraint";
                        fieldErrors.put(path, violation.getMessage());
                }

                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Validierungsfehler",
                                                fieldErrors));
        }

        @ExceptionHandler(ErrorResponseException.class)
        public ResponseEntity<ApiErrorResponse> handleErrorResponse(ErrorResponseException ex) {
                String message = ex.getBody() != null && ex.getBody().getDetail() != null
                                ? ex.getBody().getDetail()
                                : "Fehler";

                return ResponseEntity.status(ex.getStatusCode()).body(
                                new ApiErrorResponse(
                                                ex.getStatusCode().value(),
                                                message,
                                                Map.of()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                new ApiErrorResponse(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "Ein unerwarteter Fehler ist aufgetreten.",
                                                Map.of()));
        }
}