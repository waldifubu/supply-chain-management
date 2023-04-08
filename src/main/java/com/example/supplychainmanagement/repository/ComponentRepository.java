package com.example.supplychainmanagement.repository;

import com.example.supplychainmanagement.entity.Component;

import com.example.supplychainmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {

//    @Query("select o from Order o JOIN FETCH o.ordersProducts where o.orderNo = ?1")
//    Optional<Order> findOrderByOrderNo(long id);

    Component getPartByArticleNo(String articleNo);

    List<Component> findAllByProduct(Product product);
}
