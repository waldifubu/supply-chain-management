package com.example.supplychainmanagement.model.distributor;

import com.example.supplychainmanagement.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeliveredResponse {

    private Order order;

    private boolean inTime;

    private double weight;

    private boolean changed;

    public DeliveredResponse(Order o, boolean inTime, double weight) {
        this.order = o;
        this.inTime = inTime;
        this.weight = weight;
    }
}
