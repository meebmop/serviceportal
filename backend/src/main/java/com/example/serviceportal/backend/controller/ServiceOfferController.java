package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.ServiceOfferCreateDto;
import com.example.serviceportal.backend.dto.ServiceOfferResponseDto;
import com.example.serviceportal.backend.dto.ServiceOfferUpdateDto;
import com.example.serviceportal.backend.service.ServiceOfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class ServiceOfferController {

    private final ServiceOfferService serviceOfferService;

    public ServiceOfferController(ServiceOfferService serviceOfferService) {
        this.serviceOfferService = serviceOfferService;
    }

    @GetMapping
    public List<ServiceOfferResponseDto> getAllOffers() {
        return serviceOfferService.getAllOffers();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOfferResponseDto createOffer(@Valid @RequestBody ServiceOfferCreateDto createDto) {
        return serviceOfferService.createOffer(createDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceOfferResponseDto updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody ServiceOfferUpdateDto updateDto
    ) {
        return serviceOfferService.updateOffer(id, updateDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOffer(@PathVariable Long id) {
        serviceOfferService.deleteOffer(id);
    }
}