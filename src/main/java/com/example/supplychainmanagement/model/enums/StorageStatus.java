package com.example.supplychainmanagement.model.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum StorageStatus {
    @JsonEnumDefaultValue
    NOT_REQUESTED, // No request for this item at the moment

    REQUESTED, // Request exists for this item at the moment
    IN_DELIVERY // Replenishment is coming
}
