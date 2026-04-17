package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.RequestCreateDto;
import com.example.serviceportal.backend.dto.RequestResponseDto;
import com.example.serviceportal.backend.dto.StatusUpdateDto;
import com.example.serviceportal.backend.model.ServiceOffer;
import com.example.serviceportal.backend.model.ServiceRequest;
import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.RequestPriority;
import com.example.serviceportal.backend.model.enums.RequestStatus;
import com.example.serviceportal.backend.model.enums.UserRole;
import com.example.serviceportal.backend.repository.ServiceOfferRepository;
import com.example.serviceportal.backend.repository.ServiceRequestRepository;
import com.example.serviceportal.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ServiceOfferRepository serviceOfferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    @Test
    void createRequest_shouldCreateRequestWithExpectedDefaults() {
        User user = createUser(1L, "Max Mustermann", "max@test.de", UserRole.USER);
        ServiceOffer offer = createOffer(10L, "IT-Support", "Support");

        RequestCreateDto dto = new RequestCreateDto();
        dto.setServiceOfferId(10L);
        dto.setSubject("Druckerproblem");
        dto.setMessage("Mein Drucker funktioniert seit heute nicht mehr.");
        dto.setPriority("Hoch");

        when(authentication.getName()).thenReturn("max@test.de");
        when(userRepository.findByEmailIgnoreCase("max@test.de")).thenReturn(Optional.of(user));
        when(serviceOfferRepository.findById(10L)).thenReturn(Optional.of(offer));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> {
            ServiceRequest request = invocation.getArgument(0);
            setServiceRequestId(request, 99L);
            setServiceRequestTimestamps(request, LocalDateTime.now(), LocalDateTime.now());
            return request;
        });

        RequestResponseDto result = serviceRequestService.createRequest(dto, authentication);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(captor.capture());

        ServiceRequest savedRequest = captor.getValue();
        assertEquals(RequestStatus.EINGEGANGEN, savedRequest.getStatus());
        assertEquals(RequestPriority.HOCH, savedRequest.getPriority());
        assertEquals("max@test.de", savedRequest.getUpdatedBy());
        assertEquals("Druckerproblem", savedRequest.getSubject());
        assertEquals("Mein Drucker funktioniert seit heute nicht mehr.", savedRequest.getMessage());

        assertEquals(99L, result.getId());
        assertEquals("Eingegangen", result.getStatus());
        assertEquals("Hoch", result.getPriority());
        assertEquals("max@test.de", result.getUserEmail());
        assertEquals("Support", result.getCategory());
    }

    @Test
    void createRequest_shouldUseNormalPriorityWhenPriorityIsBlank() {
        User user = createUser(1L, "Max Mustermann", "max@test.de", UserRole.USER);
        ServiceOffer offer = createOffer(10L, "IT-Support", "Support");

        RequestCreateDto dto = new RequestCreateDto();
        dto.setServiceOfferId(10L);
        dto.setSubject("Passwortproblem");
        dto.setMessage("Ich kann mich nicht mehr anmelden und brauche Unterstützung.");
        dto.setPriority("   ");

        when(authentication.getName()).thenReturn("max@test.de");
        when(userRepository.findByEmailIgnoreCase("max@test.de")).thenReturn(Optional.of(user));
        when(serviceOfferRepository.findById(10L)).thenReturn(Optional.of(offer));
        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> {
            ServiceRequest request = invocation.getArgument(0);
            setServiceRequestId(request, 100L);
            setServiceRequestTimestamps(request, LocalDateTime.now(), LocalDateTime.now());
            return request;
        });

        RequestResponseDto result = serviceRequestService.createRequest(dto, authentication);

        assertEquals("Normal", result.getPriority());
    }

    @Test
    void updateStatus_shouldNormalizeBlankAdminCommentToNull() {
        User admin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User requester = createUser(2L, "User", "user@test.de", UserRole.USER);
        ServiceOffer offer = createOffer(10L, "IT-Support", "Support");

        ServiceRequest request = new ServiceRequest();
        setServiceRequestId(request, 200L);
        request.setServiceOffer(offer);
        request.setUser(requester);
        request.setSubject("Betreff");
        request.setMessage("Eine ausreichend lange Nachricht.");
        request.setStatus(RequestStatus.EINGEGANGEN);
        request.setPriority(RequestPriority.NORMAL);
        request.setAdminComment("alter Kommentar");
        request.setUpdatedBy("old@test.de");
        setServiceRequestTimestamps(request, LocalDateTime.now().minusDays(1), LocalDateTime.now());

        StatusUpdateDto dto = new StatusUpdateDto();
        dto.setStatus("Abgeschlossen");
        dto.setAdminComment("   ");

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(admin));
        when(serviceRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(serviceRequestRepository.save(any(ServiceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RequestResponseDto result = serviceRequestService.updateStatus(200L, dto, authentication);

        assertEquals(RequestStatus.ABGESCHLOSSEN, request.getStatus());
        assertNull(request.getAdminComment());
        assertEquals("admin@test.de", request.getUpdatedBy());
        assertEquals("Abgeschlossen", result.getStatus());
        assertNull(result.getAdminComment());
    }

    @Test
    void updateStatus_shouldRejectInvalidStatus() {
        User admin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User requester = createUser(2L, "User", "user@test.de", UserRole.USER);
        ServiceOffer offer = createOffer(10L, "IT-Support", "Support");

        ServiceRequest request = new ServiceRequest();
        setServiceRequestId(request, 200L);
        request.setServiceOffer(offer);
        request.setUser(requester);
        request.setSubject("Betreff");
        request.setMessage("Eine ausreichend lange Nachricht.");
        request.setStatus(RequestStatus.EINGEGANGEN);
        request.setPriority(RequestPriority.NORMAL);

        StatusUpdateDto dto = new StatusUpdateDto();
        dto.setStatus("Fertig irgendwann");
        dto.setAdminComment("ok");

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(admin));
        when(serviceRequestRepository.findById(200L)).thenReturn(Optional.of(request));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceRequestService.updateStatus(200L, dto, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals(
                "Ungültiger Status. Erlaubt sind: Eingegangen, In Bearbeitung, Abgeschlossen.",
                ex.getReason());
        verify(serviceRequestRepository, never()).save(any(ServiceRequest.class));
    }

    @Test
    void createRequest_shouldRejectInvalidPriority() {
        User user = createUser(1L, "Max Mustermann", "max@test.de", UserRole.USER);
        ServiceOffer offer = createOffer(10L, "IT-Support", "Support");

        RequestCreateDto dto = new RequestCreateDto();
        dto.setServiceOfferId(10L);
        dto.setSubject("Druckerproblem");
        dto.setMessage("Mein Drucker funktioniert seit heute nicht mehr.");
        dto.setPriority("Extrem dringend");

        when(authentication.getName()).thenReturn("max@test.de");
        when(userRepository.findByEmailIgnoreCase("max@test.de")).thenReturn(Optional.of(user));
        when(serviceOfferRepository.findById(10L)).thenReturn(Optional.of(offer));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceRequestService.createRequest(dto, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Ungültige Priorität. Erlaubt sind: Niedrig, Normal, Hoch.", ex.getReason());
        verify(serviceRequestRepository, never()).save(any(ServiceRequest.class));
    }

    @Test
    void createRequest_shouldRejectWhenOfferDoesNotExist() {
        User user = createUser(1L, "Max Mustermann", "max@test.de", UserRole.USER);

        RequestCreateDto dto = new RequestCreateDto();
        dto.setServiceOfferId(999L);
        dto.setSubject("Druckerproblem");
        dto.setMessage("Mein Drucker funktioniert seit heute nicht mehr.");
        dto.setPriority("Normal");

        when(authentication.getName()).thenReturn("max@test.de");
        when(userRepository.findByEmailIgnoreCase("max@test.de")).thenReturn(Optional.of(user));
        when(serviceOfferRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceRequestService.createRequest(dto, authentication));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Das ausgewählte Serviceangebot existiert nicht.", ex.getReason());
        verify(serviceRequestRepository, never()).save(any(ServiceRequest.class));
    }

    @Test
    void updateStatus_shouldSaveTrimmedAdminComment() {
        User admin = createUser(1L, "Admin", "admin@test.de", UserRole.ADMIN);
        User requester = createUser(2L, "User", "user@test.de", UserRole.USER);
        ServiceOffer offer = createOffer(10L, "IT-Support", "Support");

        ServiceRequest request = new ServiceRequest();
        setServiceRequestId(request, 200L);
        request.setServiceOffer(offer);
        request.setUser(requester);
        request.setSubject("Betreff");
        request.setMessage("Eine ausreichend lange Nachricht.");
        request.setStatus(RequestStatus.EINGEGANGEN);
        request.setPriority(RequestPriority.NORMAL);
        setServiceRequestTimestamps(request, LocalDateTime.now().minusDays(1), LocalDateTime.now());

        StatusUpdateDto dto = new StatusUpdateDto();
        dto.setStatus("In Bearbeitung");
        dto.setAdminComment("   Wird aktuell geprüft.   ");

        when(authentication.getName()).thenReturn("admin@test.de");
        when(userRepository.findByEmailIgnoreCase("admin@test.de")).thenReturn(Optional.of(admin));
        when(serviceRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(serviceRequestRepository.save(any(ServiceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RequestResponseDto result = serviceRequestService.updateStatus(200L, dto, authentication);

        assertEquals(RequestStatus.IN_BEARBEITUNG, request.getStatus());
        assertEquals("Wird aktuell geprüft.", request.getAdminComment());
        assertEquals("In Bearbeitung", result.getStatus());
        assertEquals("Wird aktuell geprüft.", result.getAdminComment());
    }

    @Test
    void updateStatus_shouldRejectWhenRequestDoesNotExist() {
        StatusUpdateDto dto = new StatusUpdateDto();
        dto.setStatus("Abgeschlossen");
        dto.setAdminComment("ok");

        when(serviceRequestRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceRequestService.updateStatus(999L, dto, authentication));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Anfrage nicht gefunden", ex.getReason());

        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        verify(serviceRequestRepository, never()).save(any(ServiceRequest.class));
    }

    @Test
    void createRequest_shouldRejectWhenAuthenticationIsMissing() {
        RequestCreateDto dto = new RequestCreateDto();
        dto.setServiceOfferId(10L);
        dto.setSubject("Druckerproblem");
        dto.setMessage("Mein Drucker funktioniert seit heute nicht mehr.");
        dto.setPriority("Normal");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceRequestService.createRequest(dto, null));

        assertEquals(401, ex.getStatusCode().value());
        assertEquals("Nicht angemeldet", ex.getReason());
    }

    @Test
    void getMyRequests_shouldReturnOnlyMappedUserRequests() {
        User requester = createUser(2L, "User", "user@test.de", UserRole.USER);
        ServiceOffer offer = createOffer(10L, "IT-Support", "Support");

        ServiceRequest request = new ServiceRequest();
        setServiceRequestId(request, 300L);
        request.setServiceOffer(offer);
        request.setUser(requester);
        request.setSubject("Betreff");
        request.setMessage("Eine ausreichend lange Nachricht.");
        request.setStatus(RequestStatus.EINGEGANGEN);
        request.setPriority(RequestPriority.NORMAL);
        request.setUpdatedBy("user@test.de");
        setServiceRequestTimestamps(request, LocalDateTime.now(), LocalDateTime.now());

        when(authentication.getName()).thenReturn("user@test.de");
        when(userRepository.findByEmailIgnoreCase("user@test.de")).thenReturn(Optional.of(requester));
        when(serviceRequestRepository.findByUser_IdOrderByCreatedAtDesc(eq(2L), any()))
                .thenReturn(new PageImpl<>(List.of(request)));

        var result = serviceRequestService.getMyRequests(authentication, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Betreff", result.get(0).getSubject());
        assertEquals("Eingegangen", result.get(0).getStatus());
    }

    private User createUser(Long id, String name, String email, UserRole role) {
        User user = new User();
        setUserId(user, id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash("hashed");
        return user;
    }

    private ServiceOffer createOffer(Long id, String title, String category) {
        ServiceOffer offer = new ServiceOffer();
        setServiceOfferId(offer, id);
        offer.setTitle(title);
        offer.setCategory(category);
        offer.setDescription("Beschreibung");
        return offer;
    }

    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setServiceOfferId(ServiceOffer offer, Long id) {
        try {
            var field = ServiceOffer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(offer, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setServiceRequestId(ServiceRequest request, Long id) {
        try {
            var field = ServiceRequest.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(request, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setServiceRequestTimestamps(ServiceRequest request, LocalDateTime createdAt, LocalDateTime updatedAt) {
        try {
            var createdAtField = ServiceRequest.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(request, createdAt);

            var updatedAtField = ServiceRequest.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(request, updatedAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}