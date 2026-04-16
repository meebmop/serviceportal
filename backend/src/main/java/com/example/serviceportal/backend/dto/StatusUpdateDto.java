package com.example.serviceportal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StatusUpdateDto {

    @NotBlank(message = "Der Status darf nicht leer sein.")
    private String status;

    @Size(max = 500, message = "Der interne Bearbeitungskommentar darf maximal 500 Zeichen lang sein.")
    private String adminComment;

    public StatusUpdateDto() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}