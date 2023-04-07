package com.example.supplychainmanagement.repository;

import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.OrdersProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrdersProductsRepository extends JpaRepository<OrdersProducts, Long> {

    @Query("select o from OrdersProducts o where o.order = ?1")
    List<OrdersProducts> findOrdersProductsByOrderNo(Order o);

    OrdersProducts getById(Long id);
}

