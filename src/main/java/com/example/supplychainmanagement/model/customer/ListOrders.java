package com.example.supplychainmanagement.model.customer;

import com.example.supplychainmanagement.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListOrders {

    List<Order> orders;

    int amount;
}
