package com.example.supplychainmanagement.model.distributor;

import com.example.supplychainmanagement.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeliveredResponse {

    private Order order;

    private boolean onTime;

    private double weight;
}
