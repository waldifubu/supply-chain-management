package com.example.supplychainmanagement.model.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum ProductStatus {
    @JsonEnumDefaultValue
    PENDING,
    IN_STOCK,
    OUT_OF_STOCK,
    AVAILABLE
}
