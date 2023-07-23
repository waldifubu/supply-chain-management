package com.example.supplychainmanagement.service;

import com.example.supplychainmanagement.dto.CompactOrder;
import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.OrdersProducts;
import com.example.supplychainmanagement.entity.Product;
import com.example.supplychainmanagement.repository.OrdersProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersProductsRepository ordersProductsRepository;

    public ResponseEntity<?> calculateOrder(Order order) {

        return null;
    }

    public CompactOrder createCompactOrder(Order order) {
        List<OrdersProducts> opList = ordersProductsRepository.findOrdersProductsByOrderNo(order);
        List<Product> productList = new ArrayList<>();
        for (OrdersProducts op : opList) {
            Product p = op.getProduct();
            p.setQty(op.getQty());
            p.setProductStatus(op.getStatus());
            productList.add(p);
        }

        String orderDate = order.getOrderDate() != null ? order.getOrderDate().toString() : "";
        String dueDate = order.getDueDate() != null ? order.getDueDate().toString() : "";

        boolean inTime = false;
        if (null != order.getDeliveryDate() && null != order.getDueDate()) {
            inTime = order.getDeliveryDate().isBefore(Instant.ofEpochMilli(order.getDueDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        return new CompactOrder(
                order.getId(),
                order.getOrderNo(),
                orderDate,
                order.getStatus(),
                dueDate,
                productList,
                productList.size(),
                order.getUpdated(),
                order.getUser().getName(),
                order.getDeliveryDate(),
                inTime
        );
    }
}
