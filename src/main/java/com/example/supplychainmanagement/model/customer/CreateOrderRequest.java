package com.example.supplychainmanagement.model.customer;

import com.example.supplychainmanagement.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class CreateOrderRequest {

    private String dueDate;

    private Set<Product> products;
}
