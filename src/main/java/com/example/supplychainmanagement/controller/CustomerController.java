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
import java.util.Arrays;
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
            //Alternative: Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
            User user = optionalUser.orElseThrow();

            long orderNo = (long) (Math.random() * 1337);
            Order order = new Order(orderNo);
            if (request.getDueDate() == null) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }

            try {
                Date.valueOf(request.getDueDate());
            } catch (IllegalArgumentException iae) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }

            order.setDueDate(Date.valueOf(request.getDueDate()));
            order.setOrderDate(LocalDateTime.now());
            order.setUser(user);
            orderRepository.save(order);

            for (Product requestedProduct : request.getProducts()) {
                OrdersProducts ordersProducts = new OrdersProducts();
                ordersProducts.setOrder(order);
                Product inCart = productRepository.findProductByArticleNo(requestedProduct.getArticleNo());
                ordersProducts.setProduct(inCart);
                ordersProducts.setQty(requestedProduct.getQty());
                ordersProductsRepository.save(ordersProducts);
            }

            CreateOrderResponse cor = new CreateOrderResponse(order.getOrderNo(), order.getStatus(), order.getOrderDate());
            return new ResponseEntity<>(cor, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orders")
    @Secured({"ROLE_CUSTOMER", "ROLE_ADMIN"})
    public ResponseEntity<?> getAllOrders(@AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser,
                                          @RequestParam(required = false, name = "status") Optional<String> optionalStatus) {
        boolean notFound = false;
        if (optionalStatus.isPresent()) {
            notFound = Arrays.stream(OrderStatus.values()).noneMatch(o -> o.toString().equalsIgnoreCase(optionalStatus.get()));
            if (notFound) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }

        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();
        List<Order> orderList;

        try {
            if (optionalStatus.isPresent()) {
                var status = OrderStatus.valueOf(optionalStatus.get().toUpperCase());
                orderList = orderRepository.findAllByUserAndStatus(user, status);
            } else {
                orderList = orderRepository.findAllByUser(user);
            }

            orderList = orderList.stream()
                    .filter(order -> order.getOrdersProducts() != null)
                    .peek(order -> order.setCountProducts(order.getOrdersProducts().size()))
                    .peek(order -> order.setOrdersProducts(null))
                    .toList();

            ListOrders cor = new ListOrders(orderList, orderList.size());
            return new ResponseEntity<>(cor, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
        boolean isAdmin = optionalRole.isPresent() && user.getRoles().contains(optionalRole.get());

        if (isAdmin) {
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
    public ResponseEntity<Object> closeOrder(
            @PathVariable("id") long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();
        Optional<Order> optionalOrder;
        Optional<Role> optionalAdminRole = roleRepository.findByName(UserRole.ROLE_ADMIN.toString());
        Optional<Role> optionalEnterpriseRole = roleRepository.findByName(UserRole.ROLE_ENTERPRISE.toString());

        // Check if user has role ADMIN or ENTERPRISE, else it's a customer
        boolean allowedRole = UserRole.contains(optionalAdminRole.map(Role::toString).orElse(null)) ||
                UserRole.contains(optionalEnterpriseRole.map(Role::toString).orElse(null));

        try {
            if (allowedRole) {
                optionalOrder = orderRepository.findOrderByOrderNo(id);
            } else {
                optionalOrder = orderRepository.findOrderByOrderNoAndUser(id, user);
            }

            Order order = optionalOrder.orElseThrow();
            if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.REJECTED) {
                order.setUpdated(LocalDateTime.now());
                order.setStatus(OrderStatus.CLOSED);
                orderRepository.save(order);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            order.setOrdersProducts(null);

            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}