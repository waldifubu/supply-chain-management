package com.example.supplychainmanagement.model.enterprise;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestPartsOrderRequest {
    private Long articleNo;

    private String articleCode;

    private int qty;

    private String comment;
}
