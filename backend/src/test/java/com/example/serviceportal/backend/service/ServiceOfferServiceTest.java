package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.ServiceOfferCreateDto;
import com.example.serviceportal.backend.dto.ServiceOfferResponseDto;
import com.example.serviceportal.backend.dto.ServiceOfferUpdateDto;
import com.example.serviceportal.backend.model.ServiceOffer;
import com.example.serviceportal.backend.repository.ServiceOfferRepository;
import com.example.serviceportal.backend.repository.ServiceRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOfferServiceTest {

    @Mock
    private ServiceOfferRepository serviceOfferRepository;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @InjectMocks
    private ServiceOfferService serviceOfferService;

    @Test
    void getAllOffers_shouldReturnMappedDtos() {
        ServiceOffer offer1 = createOffer(1L, "IT-Support", "Beschreibung 1", "Support");
        ServiceOffer offer2 = createOffer(2L, "Beratung", "Beschreibung 2", "Beratung");

        when(serviceOfferRepository.findAll()).thenReturn(List.of(offer1, offer2));

        List<ServiceOfferResponseDto> result = serviceOfferService.getAllOffers();

        assertEquals(2, result.size());
        assertEquals("IT-Support", result.get(0).getTitle());
        assertEquals("Beratung", result.get(1).getCategory());
    }

    @Test
    void createOffer_shouldPersistOffer() {
        ServiceOfferCreateDto dto = new ServiceOfferCreateDto();
        dto.setTitle("IT-Support");
        dto.setDescription("Beschreibung");
        dto.setCategory("Support");

        when(serviceOfferRepository.save(any(ServiceOffer.class))).thenAnswer(invocation -> {
            ServiceOffer offer = invocation.getArgument(0);
            setServiceOfferId(offer, 10L);
            return offer;
        });

        ServiceOfferResponseDto result = serviceOfferService.createOffer(dto);

        ArgumentCaptor<ServiceOffer> captor = ArgumentCaptor.forClass(ServiceOffer.class);
        verify(serviceOfferRepository).save(captor.capture());

        ServiceOffer saved = captor.getValue();
        assertEquals("IT-Support", saved.getTitle());
        assertEquals("Beschreibung", saved.getDescription());
        assertEquals("Support", saved.getCategory());

        assertEquals(10L, result.getId());
        assertEquals("IT-Support", result.getTitle());
    }

    @Test
    void updateOffer_shouldUpdateExistingOffer() {
        ServiceOffer existing = createOffer(1L, "Alt", "Alt Beschreibung", "Alt Kategorie");

        ServiceOfferUpdateDto dto = new ServiceOfferUpdateDto();
        dto.setTitle("Neu");
        dto.setDescription("Neue Beschreibung");
        dto.setCategory("Neue Kategorie");

        when(serviceOfferRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceOfferRepository.save(any(ServiceOffer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOfferResponseDto result = serviceOfferService.updateOffer(1L, dto);

        assertEquals("Neu", existing.getTitle());
        assertEquals("Neue Beschreibung", existing.getDescription());
        assertEquals("Neue Kategorie", existing.getCategory());

        assertEquals("Neu", result.getTitle());
        assertEquals("Neue Kategorie", result.getCategory());
    }

    @Test
    void updateOffer_shouldRejectMissingOffer() {
        ServiceOfferUpdateDto dto = new ServiceOfferUpdateDto();
        dto.setTitle("Neu");
        dto.setDescription("Neue Beschreibung");
        dto.setCategory("Neue Kategorie");

        when(serviceOfferRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceOfferService.updateOffer(99L, dto));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Serviceangebot nicht gefunden", ex.getReason());
    }

    @Test
    void deleteOffer_shouldDeleteWhenUnused() {
        when(serviceOfferRepository.existsById(1L)).thenReturn(true);
        when(serviceRequestRepository.existsByServiceOffer_Id(1L)).thenReturn(false);

        serviceOfferService.deleteOffer(1L);

        verify(serviceOfferRepository).deleteById(1L);
    }

    @Test
    void deleteOffer_shouldRejectMissingOffer() {
        when(serviceOfferRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceOfferService.deleteOffer(1L));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Serviceangebot nicht gefunden", ex.getReason());
        verify(serviceOfferRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteOffer_shouldRejectWhenRequestsExist() {
        when(serviceOfferRepository.existsById(1L)).thenReturn(true);
        when(serviceRequestRepository.existsByServiceOffer_Id(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> serviceOfferService.deleteOffer(1L));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals(
                "Das Serviceangebot kann nicht gelöscht werden, weil noch Anfragen darauf verweisen.",
                ex.getReason());
        verify(serviceOfferRepository, never()).deleteById(anyLong());
    }

    private ServiceOffer createOffer(Long id, String title, String description, String category) {
        ServiceOffer offer = new ServiceOffer();
        setServiceOfferId(offer, id);
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setCategory(category);
        return offer;
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
}