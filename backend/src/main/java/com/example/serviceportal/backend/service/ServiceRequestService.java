package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.RequestCreateDto;
import com.example.serviceportal.backend.dto.RequestResponseDto;
import com.example.serviceportal.backend.dto.StatusUpdateDto;
import com.example.serviceportal.backend.model.ServiceOffer;
import com.example.serviceportal.backend.model.ServiceRequest;
import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.RequestPriority;
import com.example.serviceportal.backend.model.enums.RequestStatus;
import com.example.serviceportal.backend.repository.ServiceOfferRepository;
import com.example.serviceportal.backend.repository.ServiceRequestRepository;
import com.example.serviceportal.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final UserRepository userRepository;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository,
            ServiceOfferRepository serviceOfferRepository,
            UserRepository userRepository
    ) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceOfferRepository = serviceOfferRepository;
        this.userRepository = userRepository;
    }

    public RequestResponseDto createRequest(RequestCreateDto requestDto, Authentication authentication) {
        User user = findAuthenticatedUser(authentication);
        ServiceOffer serviceOffer = findOfferByIdOrThrow(requestDto.getServiceOfferId());

        RequestPriority priority = parsePriority(requestDto.getPriority());

        ServiceRequest request = new ServiceRequest();
        request.setServiceOffer(serviceOffer);
        request.setUser(user);
        request.setSubject(requestDto.getSubject());
        request.setMessage(requestDto.getMessage());
        request.setStatus(RequestStatus.EINGEGANGEN);
        request.setPriority(priority);
        request.setAdminComment(null);
        request.setUpdatedBy(user.getEmail());

        ServiceRequest savedRequest = serviceRequestRepository.save(request);
        return mapToDto(savedRequest);
    }

    public List<RequestResponseDto> getAllRequests(int page, int size) {
        return serviceRequestRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<RequestResponseDto> getMyRequests(Authentication authentication, int page, int size) {
        User user = findAuthenticatedUser(authentication);

        return serviceRequestRepository.findByUser_IdOrderByCreatedAtDesc(
                        user.getId(),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public RequestResponseDto updateStatus(
            Long id,
            StatusUpdateDto statusUpdateDto,
            Authentication authentication
    ) {
        ServiceRequest request = findRequestByIdOrThrow(id);
        User admin = findAuthenticatedUser(authentication);

        RequestStatus status = parseStatus(statusUpdateDto.getStatus());

        request.setStatus(status);
        request.setAdminComment(normalizeAdminComment(statusUpdateDto.getAdminComment()));
        request.setUpdatedBy(admin.getEmail());

        ServiceRequest savedRequest = serviceRequestRepository.save(request);
        return mapToDto(savedRequest);
    }

    private User findAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht angemeldet");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nicht angemeldet"));
    }

    private ServiceOffer findOfferByIdOrThrow(Long offerId) {
        return serviceOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Das ausgewählte Serviceangebot existiert nicht."
                ));
    }

    private ServiceRequest findRequestByIdOrThrow(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anfrage nicht gefunden"));
    }

    private RequestStatus parseStatus(String status) {
        try {
            return RequestStatus.fromValue(status);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ungültiger Status. Erlaubt sind: Eingegangen, In Bearbeitung, Abgeschlossen."
            );
        }
    }

    private RequestPriority parsePriority(String priority) {
        try {
            return RequestPriority.fromValue(priority);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ungültige Priorität. Erlaubt sind: Niedrig, Normal, Hoch."
            );
        }
    }

    private String normalizeAdminComment(String adminComment) {
        if (adminComment == null) {
            return null;
        }

        String normalized = adminComment.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private RequestResponseDto mapToDto(ServiceRequest request) {
        return new RequestResponseDto(
                request.getId(),
                request.getServiceOffer().getId(),
                request.getUser().getId(),
                request.getSubject(),
                request.getServiceOffer().getCategory(),
                request.getUser().getEmail(),
                request.getMessage(),
                request.getStatus().toString(),
                request.getPriority().toString(),
                request.getAdminComment(),
                request.getUpdatedBy(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}