package com.example.serviceportal.backend.dto;

public class ServiceOfferResponseDto {

    private Long id;
    private String title;
    private String description;
    private String category;

    public ServiceOfferResponseDto() {
    }

    public ServiceOfferResponseDto(Long id, String title, String description, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }
}