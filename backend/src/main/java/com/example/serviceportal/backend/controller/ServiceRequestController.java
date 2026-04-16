package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.RequestCreateDto;
import com.example.serviceportal.backend.dto.RequestResponseDto;
import com.example.serviceportal.backend.dto.StatusUpdateDto;
import com.example.serviceportal.backend.service.ServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponseDto createRequest(
            @Valid @RequestBody RequestCreateDto requestDto,
            Authentication authentication
    ) {
        return serviceRequestService.createRequest(requestDto, authentication);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RequestResponseDto> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return serviceRequestService.getAllRequests(page, size);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public List<RequestResponseDto> getMyRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return serviceRequestService.getMyRequests(authentication, page, size);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public RequestResponseDto updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDto statusUpdateDto,
            Authentication authentication
    ) {
        return serviceRequestService.updateStatus(id, statusUpdateDto, authentication);
    }
}