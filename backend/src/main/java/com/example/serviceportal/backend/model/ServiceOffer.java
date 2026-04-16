package com.example.serviceportal.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class ServiceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Der Titel darf nicht leer sein.")
    @Size(min = 3, max = 100, message = "Der Titel muss zwischen 3 und 100 Zeichen lang sein.")
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank(message = "Die Beschreibung darf nicht leer sein.")
    @Size(min = 10, max = 500, message = "Die Beschreibung muss zwischen 10 und 500 Zeichen lang sein.")
    @Column(nullable = false, length = 500)
    private String description;

    @NotBlank(message = "Die Kategorie darf nicht leer sein.")
    @Size(max = 100, message = "Die Kategorie darf maximal 100 Zeichen lang sein.")
    @Column(nullable = false, length = 100)
    private String category;

    public ServiceOffer() {
    }

    public Long getId() {
        return id;
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