package com.example.supplychainmanagement.model.enums;

public enum StorageStatus {
    NOT_REQUESTED, // No request for this item at the moment
    REQUESTED, // Request exists for this item at the moment
    IN_DELIVERY // Replenishment is coming
}
