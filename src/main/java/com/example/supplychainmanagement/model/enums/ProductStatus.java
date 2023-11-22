package com.example.supplychainmanagement.model.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ProductStatus {
    @JsonEnumDefaultValue
    PENDING, // Intial status. Request for availability
    IN_STOCK, // Product is in stock
    OUT_OF_STOCK, // Product is out of stock
    AVAILABLE // Only if all needed products are "in stock", they become available
}
