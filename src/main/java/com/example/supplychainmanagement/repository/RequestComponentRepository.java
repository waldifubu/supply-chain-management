package com.example.supplychainmanagement.repository;

import com.example.supplychainmanagement.entity.RequestComponent;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestComponentRepository extends JpaRepository<RequestComponent, Long> {
    List<RequestComponent> findAllByRequestStatusOrderByRequestDate(RequestStatus status);

    Optional<RequestComponent> getById(UUID id);

    List<RequestComponent> findAllByRequestStatusAndSupplier(RequestStatus requestStatus, User supplier);
}
