package com.example.serviceportal.backend.model;

import com.example.serviceportal.backend.model.enums.RequestPriority;
import com.example.serviceportal.backend.model.enums.RequestStatus;
import com.example.serviceportal.backend.persistence.RequestPriorityConverter;
import com.example.serviceportal.backend.persistence.RequestStatusConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_offer_id", nullable = false)
    private ServiceOffer serviceOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Der Betreff darf nicht leer sein.")
    @Size(min = 3, max = 100, message = "Der Betreff muss zwischen 3 und 100 Zeichen lang sein.")
    @Column(nullable = false, length = 100)
    private String subject;

    @NotBlank(message = "Die Nachricht darf nicht leer sein.")
    @Size(min = 10, max = 1000, message = "Die Nachricht muss zwischen 10 und 1000 Zeichen lang sein.")
    @Column(nullable = false, length = 1000)
    private String message;

    @Convert(converter = RequestStatusConverter.class)
    @Column(nullable = false, length = 50)
    private RequestStatus status = RequestStatus.EINGEGANGEN;

    @Convert(converter = RequestPriorityConverter.class)
    @Column(nullable = false, length = 20)
    private RequestPriority priority = RequestPriority.NORMAL;

    @Size(max = 500, message = "Der interne Bearbeitungskommentar darf maximal 500 Zeichen lang sein.")
    @Column(length = 500)
    private String adminComment;

    @Column(length = 255)
    private String updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ServiceRequest() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ServiceOffer getServiceOffer() {
        return serviceOffer;
    }

    public void setServiceOffer(ServiceOffer serviceOffer) {
        this.serviceOffer = serviceOffer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject == null ? null : subject.trim();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment == null ? null : adminComment.trim();
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy == null ? null : updatedBy.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}