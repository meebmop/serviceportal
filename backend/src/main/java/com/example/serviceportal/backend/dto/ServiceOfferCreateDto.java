package com.example.serviceportal.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ServiceOfferCreateDto {

    @NotBlank(message = "Der Titel darf nicht leer sein.")
    @Size(min = 3, max = 100, message = "Der Titel muss zwischen 3 und 100 Zeichen lang sein.")
    private String title;

    @NotBlank(message = "Die Beschreibung darf nicht leer sein.")
    @Size(min = 10, max = 500, message = "Die Beschreibung muss zwischen 10 und 500 Zeichen lang sein.")
    private String description;

    @NotBlank(message = "Die Kategorie darf nicht leer sein.")
    @Size(max = 100, message = "Die Kategorie darf maximal 100 Zeichen lang sein.")
    private String category;

    public ServiceOfferCreateDto() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null ? null : category.trim();
    }
}