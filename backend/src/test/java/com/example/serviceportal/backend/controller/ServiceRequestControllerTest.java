package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.RequestCreateDto;
import com.example.serviceportal.backend.dto.RequestResponseDto;
import com.example.serviceportal.backend.dto.StatusUpdateDto;
import com.example.serviceportal.backend.service.ServiceRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestControllerTest {

    @Mock
    private ServiceRequestService serviceRequestService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ServiceRequestController serviceRequestController;

    @Test
    void createRequest_shouldDelegateToService() {
        RequestCreateDto dto = new RequestCreateDto();
        dto.setServiceOfferId(10L);
        dto.setSubject("Druckerproblem");
        dto.setMessage("Mein Drucker funktioniert nicht mehr.");
        dto.setPriority("Normal");

        RequestResponseDto expected = createResponseDto(1L, "Druckerproblem", "Eingegangen");

        when(serviceRequestService.createRequest(dto, authentication)).thenReturn(expected);

        RequestResponseDto result = serviceRequestController.createRequest(dto, authentication);

        assertEquals(1L, result.getId());
        assertEquals("Eingegangen", result.getStatus());
        verify(serviceRequestService).createRequest(dto, authentication);
    }

    @Test
    void getAllRequests_shouldDelegateToService() {
        List<RequestResponseDto> expected = List.of(
                createResponseDto(1L, "Betreff 1", "Eingegangen"),
                createResponseDto(2L, "Betreff 2", "Abgeschlossen"));

        when(serviceRequestService.getAllRequests(0, 20)).thenReturn(expected);

        List<RequestResponseDto> result = serviceRequestController.getAllRequests(0, 20);

        assertEquals(2, result.size());
        verify(serviceRequestService).getAllRequests(0, 20);
    }

    @Test
    void getMyRequests_shouldDelegateToService() {
        List<RequestResponseDto> expected = List.of(
                createResponseDto(1L, "Mein Betreff", "In Bearbeitung"));

        when(serviceRequestService.getMyRequests(authentication, 0, 10)).thenReturn(expected);

        List<RequestResponseDto> result = serviceRequestController.getMyRequests(authentication, 0, 10);

        assertEquals(1, result.size());
        assertEquals("In Bearbeitung", result.get(0).getStatus());
        verify(serviceRequestService).getMyRequests(authentication, 0, 10);
    }

    @Test
    void updateStatus_shouldDelegateToService() {
        StatusUpdateDto dto = new StatusUpdateDto();
        dto.setStatus("Abgeschlossen");
        dto.setAdminComment("Erledigt");

        RequestResponseDto expected = createResponseDto(1L, "Betreff", "Abgeschlossen");

        when(serviceRequestService.updateStatus(1L, dto, authentication)).thenReturn(expected);

        RequestResponseDto result = serviceRequestController.updateStatus(1L, dto, authentication);

        assertEquals("Abgeschlossen", result.getStatus());
        verify(serviceRequestService).updateStatus(1L, dto, authentication);
    }

    private RequestResponseDto createResponseDto(Long id, String subject, String status) {
        return new RequestResponseDto(
                id,
                10L,
                20L,
                subject,
                "Support",
                "user@test.de",
                "Nachricht",
                status,
                "Normal",
                null,
                "admin@test.de",
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}