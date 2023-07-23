package com.example.supplychainmanagement.model.enums;

public enum RequestStatus {
    OPEN(0), // Just created
    APPROVED(1), // Supplier tells request is approved
    IN_TRANSIT(2), // Supplier tells request is on the way
    DELIVERED(3), //
    STORAGE(4);

    RequestStatus(int i) {

    }
}
