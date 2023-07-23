package com.example.supplychainmanagement.repository;

import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.OrdersProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersProductsRepository extends JpaRepository<OrdersProducts, Long> {

    @Query("select o from OrdersProducts o where o.order = ?1")
    List<OrdersProducts> findOrdersProductsByOrderNo(Order o);

//    @Query("select o from Order o where o.id = ?1")
//    OrdersProducts getById(Long id);
}

