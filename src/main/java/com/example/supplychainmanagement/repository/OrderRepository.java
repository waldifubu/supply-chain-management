package com.example.supplychainmanagement.repository;

import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUser(User user);

    List<Order> findAllByUserAndStatus(User user, OrderStatus status);

    @Query("select o from Order o where o.orderNo = ?1 and o.user = ?2")
    Optional<Order> findOrderByOrderNoAndUser(long id, User user);

    @Query("select o from Order o JOIN FETCH o.ordersProducts where o.orderNo = ?1")
    Optional<Order> findOrderByOrderNo(long id);

    @Query("select o from Order o where o.status = ?1")
    List<Order> findOrderByStatus(OrderStatus status);


    @Query("select o from Order o where o.status = ?1 and DATE(o.orderDate) = ?2")
    List<Order> findOrderByStatusAndOrderDate(OrderStatus status, Date date);


    Optional<Order> getOrderByOrderNo(long id);

    Optional<Order> getOrderByOrderNoAndDistributor(long id, User user);
}
