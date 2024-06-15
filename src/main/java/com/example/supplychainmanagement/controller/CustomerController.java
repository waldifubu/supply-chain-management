package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.dto.CompactOrder;
import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.Role;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.customer.CreateOrderRequest;
import com.example.supplychainmanagement.model.customer.CreateOrderResponse;
import com.example.supplychainmanagement.model.customer.ListOrders;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.example.supplychainmanagement.model.enums.UserRole;
import com.example.supplychainmanagement.repository.OrderRepository;
import com.example.supplychainmanagement.repository.RoleRepository;
import com.example.supplychainmanagement.repository.UserRepository;
import com.example.supplychainmanagement.service.business.OrderService;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class CustomerController {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final OrderService orderService;

    /**
     * Create order
     *
     * @param request Request body
     * @param authUser Auth user
     * @return Order confirmation
     */
    @PostMapping("/order")
    @Secured({"ROLE_CUSTOMER", "ROLE_ADMIN"})
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody(required = true) CreateOrderRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser) {
        try {
            //Or: Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
            User user = optionalUser.orElseThrow();

            if (request.getDueDate() == null) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }

            try {
                Date.valueOf(request.getDueDate());
            } catch (IllegalArgumentException iae) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }

            Order order = orderService.createOrder(request, user);
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
        if (optionalStatus.isPresent()) {
            boolean notFound = Arrays.stream(OrderStatus.values()).noneMatch(o -> o.toString().equalsIgnoreCase(optionalStatus.get()));
            if (notFound) {
                return ResponseEntity.notFound().build();
            }
        }

        try {
            Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
            User user = optionalUser.orElseThrow();
            OrderStatus status = optionalStatus.map(s -> OrderStatus.valueOf(s.toUpperCase())).orElse(OrderStatus.OPEN);

            List<Order> orderList = orderService.getOrderByUserAndStatus(user, status);

            // Change output: First count all products, then remove from array
            var newOrderList = orderList.stream()
                    .filter(order -> order.getOrdersProducts() != null)
                    .peek(order -> order.setCountProducts(order.getOrdersProducts().size()))
                    .peek(order -> order.setOrdersProducts(null))
                    .toList();

            ListOrders cor = new ListOrders(newOrderList);
            return new ResponseEntity<>(cor, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get certain order with order no
     *
     * @param id Order id
     * @param authUser Logged in user
     * @return Order
     */
    @GetMapping(value = "/order/{id}")
    @Secured({"ROLE_CUSTOMER", "ROLE_ADMIN"})
    public ResponseEntity<Object> getOneOrderByNo(
            @PathVariable("id") long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
            User user = optionalUser.orElseThrow();
            Optional<Order> orderData;
            Optional<Role> optionalRole = roleRepository.findByName(UserRole.ROLE_ADMIN.name());
            boolean isAdmin = optionalRole.isPresent() && user.getRoles().contains(optionalRole.get());
            SimpleBeanPropertyFilter propertyFilter;

            if (isAdmin) {
                orderData = orderService.getOrder(id);
                propertyFilter = SimpleBeanPropertyFilter.serializeAll();
            } else {
                orderData = orderService.getOrder(id, user);
                // Properties to hide
                Set<String> includedFields = new HashSet<>();
                includedFields.add("orderId");
                includedFields.add("creatorName");
                propertyFilter = SimpleBeanPropertyFilter.serializeAllExcept(includedFields);
            }

            Order order = orderData.orElseThrow();
            CompactOrder compactOrder = orderService.createCompactOrder(order);

            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("filterAdmin", propertyFilter);
            MappingJacksonValue jacksonValue = new MappingJacksonValue(compactOrder);
            jacksonValue.setFilters(filterProvider);

            return ResponseEntity.status(HttpStatus.OK).body(jacksonValue);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();

        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Close order
     *
     * @param id
     * @param authUser
     * @return
     */
    @GetMapping(value = "/close/{id}")
    @Secured({"ROLE_CUSTOMER", "ROLE_ENTERPRISE", "ROLE_ADMIN"})
    public ResponseEntity<Object> closeOrder(
            @PathVariable("id") long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();

        try {
            var optionalOrder =  orderService.getOrder(id);
            var beforeOrder = optionalOrder.orElseThrow();
            LocalDateTime beforeChange = beforeOrder.getUpdated();

            Order order = orderService.closeOrder(id, user);
            order.setOrdersProducts(null);

            var afterChange = order.getUpdated();
            var status = beforeChange.equals(afterChange) ? HttpStatus.ALREADY_REPORTED : HttpStatus.CREATED;

            return new ResponseEntity<>(order, status);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}