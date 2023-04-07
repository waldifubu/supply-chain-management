package com.example.supplychainmanagement.model.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum OrderStatus {
    @JsonEnumDefaultValue
    CREATED,
    OPEN,
    REVIEW,
    APPROVED,
    REJECTED,
    IN_COMMISSION,
    CONVEYABLE,
    IN_TRANSIT,
    DELIVERED,
    // Kunde meldet auftrag abgeschlossen
    CLOSED
}
