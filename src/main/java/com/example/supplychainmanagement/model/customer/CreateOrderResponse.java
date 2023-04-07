package com.example.supplychainmanagement.model.customer;

import com.example.supplychainmanagement.model.enums.OrderStatus;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateOrderResponse {
    private long orderNo;

    private OrderStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime created;
}
