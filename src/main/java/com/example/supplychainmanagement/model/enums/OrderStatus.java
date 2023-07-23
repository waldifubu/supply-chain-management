package com.example.supplychainmanagement.model.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum OrderStatus {
    @JsonEnumDefaultValue
    CREATED, // Initial status
    OPEN, // If order is visited by enterprise users after creating
    REVIEW, // Checking if order is possible, run inventory check
    APPROVED, // All needed products are in stock. Order is approved, process will go on
    REJECTED, // Order is rejected by enterprise user
    IN_COMMISSION, // Order is in preparation
    CONVEYABLE, // Order is ready for distributor
    IN_TRANSIT, // Distributor tells order is on the way
    DELIVERED, // Distributor tells delivery to customer is done
    CLOSED // Customer tells order is completed
}
