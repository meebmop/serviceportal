package com.example.serviceportal.backend.dto;

import java.time.LocalDateTime;

public class RequestResponseDto {

    private final Long id;
    private final Long serviceOfferId;
    private final Long userId;
    private final String subject;
    private final String category;
    private final String userEmail;
    private final String message;
    private final String status;
    private final String priority;
    private final String adminComment;
    private final String updatedBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RequestResponseDto(
            Long id,
            Long serviceOfferId,
            Long userId,
            String subject,
            String category,
            String userEmail,
            String message,
            String status,
            String priority,
            String adminComment,
            String updatedBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.serviceOfferId = serviceOfferId;
        this.userId = userId;
        this.subject = subject;
        this.category = category;
        this.userEmail = userEmail;
        this.message = message;
        this.status = status;
        this.priority = priority;
        this.adminComment = adminComment;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getServiceOfferId() {
        return serviceOfferId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSubject() {
        return subject;
    }

    public String getCategory() {
        return category;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}