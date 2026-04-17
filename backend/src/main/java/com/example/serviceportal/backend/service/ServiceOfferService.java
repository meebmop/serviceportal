package com.example.serviceportal.backend.service;

import com.example.serviceportal.backend.dto.ServiceOfferCreateDto;
import com.example.serviceportal.backend.dto.ServiceOfferResponseDto;
import com.example.serviceportal.backend.dto.ServiceOfferUpdateDto;
import com.example.serviceportal.backend.model.ServiceOffer;
import com.example.serviceportal.backend.repository.ServiceOfferRepository;
import com.example.serviceportal.backend.repository.ServiceRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceOfferService {

    private final ServiceOfferRepository serviceOfferRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public ServiceOfferService(
            ServiceOfferRepository serviceOfferRepository,
            ServiceRequestRepository serviceRequestRepository) {
        this.serviceOfferRepository = serviceOfferRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public List<ServiceOfferResponseDto> getAllOffers() {
        return serviceOfferRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public ServiceOfferResponseDto createOffer(ServiceOfferCreateDto createDto) {
        ServiceOffer serviceOffer = new ServiceOffer();

        serviceOffer.setTitle(createDto.getTitle());
        serviceOffer.setDescription(createDto.getDescription());
        serviceOffer.setCategory(createDto.getCategory());

        ServiceOffer savedOffer = serviceOfferRepository.save(serviceOffer);

        return mapToDto(savedOffer);
    }

    public ServiceOfferResponseDto updateOffer(Long id, ServiceOfferUpdateDto updateDto) {
        ServiceOffer existingOffer = serviceOfferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviceangebot nicht gefunden"));

        existingOffer.setTitle(updateDto.getTitle());
        existingOffer.setDescription(updateDto.getDescription());
        existingOffer.setCategory(updateDto.getCategory());

        ServiceOffer savedOffer = serviceOfferRepository.save(existingOffer);

        return mapToDto(savedOffer);
    }

    public void deleteOffer(Long id) {
        if (!serviceOfferRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviceangebot nicht gefunden");
        }

        if (serviceRequestRepository.existsByServiceOffer_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Das Serviceangebot kann nicht gelöscht werden, weil noch Anfragen darauf verweisen.");
        }

        serviceOfferRepository.deleteById(id);
    }

    private ServiceOfferResponseDto mapToDto(ServiceOffer serviceOffer) {
        return new ServiceOfferResponseDto(
                serviceOffer.getId(),
                serviceOffer.getTitle(),
                serviceOffer.getDescription(),
                serviceOffer.getCategory());
    }
}