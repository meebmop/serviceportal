package com.example.serviceportal.backend.repository;

import com.example.serviceportal.backend.model.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
}