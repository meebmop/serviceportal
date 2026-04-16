package com.example.serviceportal.backend.repository;

import com.example.serviceportal.backend.model.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    Page<ServiceRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ServiceRequest> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByServiceOffer_Id(Long serviceOfferId);

    boolean existsByUser_Id(Long userId);
}