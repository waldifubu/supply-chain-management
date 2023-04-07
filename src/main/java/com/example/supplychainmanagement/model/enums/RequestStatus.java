package com.example.supplychainmanagement.model.enums;

public enum RequestStatus {
    OPEN(0),
    APPROVED(1),
    IN_TRANSIT(2),
    DELIVERED(3),
    STORAGE(4);

    RequestStatus(int i) {

    }
}
