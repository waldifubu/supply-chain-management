package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.dto.CompactOrder;
import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.OrdersProducts;
import com.example.supplychainmanagement.entity.Product;
import com.example.supplychainmanagement.entity.Role;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.customer.CreateOrderRequest;
import com.example.supplychainmanagement.model.customer.CreateOrderResponse;
import com.example.supplychainmanagement.model.customer.ListOrders;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.example.supplychainmanagement.model.enums.UserRole;
import com.example.supplychainmanagement.repository.*;
import com.example.supplychainmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class CustomerController {

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    private final OrdersProductsRepository ordersProductsRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final OrderService orderService;

    @PostMapping("/order")
    @Secured({"ROLE_CUSTOMER", "ROLE_ADMIN"})
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {
        try {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
            User user = optionalUser.orElseThrow();

            long orderNo = (long) (Math.random() * 1337);
            Order order = new Order(orderNo);
            if (request.getDueDate() == null) {
                return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            try {
                Date.valueOf(request.getDueDate());
            } catch (IllegalArgumentException iae) {
                return new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            order.setDueDate(Date.valueOf(request.getDueDate()));
            order.setOrderDate(LocalDateTime.now());
            order.setUser(user);
            orderRepository.save(order);

            for (Product requestedProduct : request.getProducts()) {
                OrdersProducts ordersProducts = new OrdersProducts();
                ordersProducts.setOrder(order);
                Product inCart = productRepository.getProductByArticleNo(requestedProduct.getArticleNo());
                ordersProducts.setProduct(inCart);
                ordersProducts.setQty(requestedProduct.getQty());
                ordersProductsRepository.save(ordersProducts);
            }

            CreateOrderResponse cor = new CreateOrderResponse(order.getOrderNo(), order.getStatus(), order.getOrderDate());
            return new ResponseEntity<>(cor, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //@TODO: Try avoid product info, just brief data
    @GetMapping("/orders")
    @Secured({"ROLE_CUSTOMER", "ROLE_ADMIN"})
    public ResponseEntity<?> showAll(@AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();

        try {
            List<Order> orderList = orderRepository.findAllByUser(user);
            List<Order> newList = new ArrayList<>();

            for (Iterator<Order> it = orderList.iterator(); it.hasNext(); ) {
                Order o = it.next();
                o.setCountProducts(o.getOrdersProducts().size());
                o.setOrdersProducts(null);
                newList.add(o);
            }
            ListOrders cor = new ListOrders(newList, orderList.size());
            return new ResponseEntity<>(cor, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/order/{id}")
    @Secured({"ROLE_CUSTOMER", "ROLE_ADMIN"})
    public ResponseEntity<CompactOrder> getOrderByNo(
            @PathVariable("id") long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();
        Optional<Order> orderData;
        Optional<Role> optionalRole = roleRepository.findByName(UserRole.ROLE_ADMIN.name());

        if (optionalRole.isPresent() && user.getRoles().contains(optionalRole.get())) {
            orderData = orderRepository.findOrderByOrderNo(id);
        } else {
            orderData = orderRepository.findOrderByOrderNoAndUser(id, user);
        }

        Order order = orderData.orElseThrow();
        CompactOrder compactOrder = orderService.createCompactOrder(order);

        try {
            return new ResponseEntity<>(compactOrder, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/close/{id}")
    @Secured({"ROLE_CUSTOMER", "ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<?> closeOrder(
            @PathVariable("id") long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();
        Optional<Order> orderData;
        Optional<Role> optionalAdminRole = roleRepository.findByName(UserRole.ROLE_ADMIN.toString());
        Optional<Role> optionalEnterpriseRole = roleRepository.findByName(UserRole.ROLE_ENTERPRISE.toString());

        boolean allowedRole = UserRole.contains(optionalAdminRole.map(Role::toString).orElse(null)) ||
                UserRole.contains(optionalEnterpriseRole.map(Role::toString).orElse(null));

        try {
            if (allowedRole) {
                orderData = orderRepository.findOrderByOrderNo(id);
            } else {
                orderData = orderRepository.findOrderByOrderNoAndUser(id, user);
            }

            Order order = orderData.orElseThrow();
            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.REJECTED) {
                order.setUpdated(LocalDateTime.now());
                order.setStatus(OrderStatus.CLOSED);
                orderRepository.save(order);
            } else {
                return new ResponseEntity<>("Order was not found", HttpStatus.NOT_FOUND);
            }

            order.setOrdersProducts(null);

            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}