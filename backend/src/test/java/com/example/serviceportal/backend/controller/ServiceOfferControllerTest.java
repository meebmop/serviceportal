package com.example.serviceportal.backend.controller;

import com.example.serviceportal.backend.dto.ServiceOfferCreateDto;
import com.example.serviceportal.backend.dto.ServiceOfferResponseDto;
import com.example.serviceportal.backend.dto.ServiceOfferUpdateDto;
import com.example.serviceportal.backend.service.ServiceOfferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOfferControllerTest {

    @Mock
    private ServiceOfferService serviceOfferService;

    @InjectMocks
    private ServiceOfferController serviceOfferController;

    @Test
    void getAllOffers_shouldDelegateToService() {
        List<ServiceOfferResponseDto> expected = List.of(
                new ServiceOfferResponseDto(1L, "IT-Support", "Beschreibung 1", "Support"),
                new ServiceOfferResponseDto(2L, "Beratung", "Beschreibung 2", "Beratung")
        );

        when(serviceOfferService.getAllOffers()).thenReturn(expected);

        List<ServiceOfferResponseDto> result = serviceOfferController.getAllOffers();

        assertEquals(2, result.size());
        assertEquals("IT-Support", result.get(0).getTitle());
        assertEquals("Beratung", result.get(1).getCategory());
        verify(serviceOfferService).getAllOffers();
    }

    @Test
    void createOffer_shouldDelegateToService() {
        ServiceOfferCreateDto dto = new ServiceOfferCreateDto();
        dto.setTitle("IT-Support");
        dto.setDescription("Beschreibung");
        dto.setCategory("Support");

        ServiceOfferResponseDto expected =
                new ServiceOfferResponseDto(1L, "IT-Support", "Beschreibung", "Support");

        when(serviceOfferService.createOffer(dto)).thenReturn(expected);

        ServiceOfferResponseDto result = serviceOfferController.createOffer(dto);

        assertEquals(1L, result.getId());
        assertEquals("IT-Support", result.getTitle());
        assertEquals("Support", result.getCategory());
        verify(serviceOfferService).createOffer(dto);
    }

    @Test
    void updateOffer_shouldDelegateToService() {
        ServiceOfferUpdateDto dto = new ServiceOfferUpdateDto();
        dto.setTitle("Neuer Titel");
        dto.setDescription("Neue Beschreibung");
        dto.setCategory("Neue Kategorie");

        ServiceOfferResponseDto expected =
                new ServiceOfferResponseDto(1L, "Neuer Titel", "Neue Beschreibung", "Neue Kategorie");

        when(serviceOfferService.updateOffer(1L, dto)).thenReturn(expected);

        ServiceOfferResponseDto result = serviceOfferController.updateOffer(1L, dto);

        assertEquals(1L, result.getId());
        assertEquals("Neuer Titel", result.getTitle());
        assertEquals("Neue Kategorie", result.getCategory());
        verify(serviceOfferService).updateOffer(1L, dto);
    }

    @Test
    void deleteOffer_shouldDelegateToService() {
        serviceOfferController.deleteOffer(1L);

        verify(serviceOfferService).deleteOffer(1L);
    }
}