package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.OrdersProducts;
import com.example.supplychainmanagement.entity.Role;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.customer.ListOrders;
import com.example.supplychainmanagement.model.distributor.DeliveredResponse;
import com.example.supplychainmanagement.model.distributor.TransitRequest;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.example.supplychainmanagement.model.enums.UserRole;
import com.example.supplychainmanagement.repository.OrderRepository;
import com.example.supplychainmanagement.repository.RoleRepository;
import com.example.supplychainmanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/distributor", produces = MediaType.APPLICATION_JSON_VALUE)
public class DistributorController {

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final RoleRepository roleRepository;

    @GetMapping("/orders")
    @Secured({"ROLE_DISTRIBUTOR", "ROLE_ADMIN"})
    public ResponseEntity<?> showConveyableOrders() {
        try {
            List<Order> orderList = orderRepository.findAllByOrderStatus(OrderStatus.CONVEYABLE);
            ListOrders cor = new ListOrders(orderList, orderList.size());
            return new ResponseEntity<>(cor, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/send/{id}")
    @Secured({"ROLE_DISTRIBUTOR"})
    public ResponseEntity<?> transitOrder(
            @PathVariable Long id,
            @RequestBody TransitRequest transitRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();

        Optional<Order> optionalOrder = orderRepository.getOrderByOrderNo(id);
        Order order = optionalOrder.orElseThrow();
        boolean inTime = false;
        double weight = 0;

        try {
            if (order.getStatus() == OrderStatus.CONVEYABLE) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(transitRequest.getDeliveryDateString(), formatter);
                order.setDeliveryDate(dateTime);
                order.setStatus(OrderStatus.IN_TRANSIT);
                order.setUpdated(LocalDateTime.now());
                order.setDistributor(user);
                orderRepository.save(order);

                for (OrdersProducts op : order.getOrdersProducts()) {
                    weight += op.getProduct().getWeight();
                }
            } else {
                handleWrongData("Wrong status of order: " + order.getStatus());
            }

            DeliveredResponse response = new DeliveredResponse(order, inTime, weight);
            order.setOrdersProducts(null);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return handleWrongData(e.getMessage());
        }
    }

    @GetMapping("/delivered/{id}")
    @Secured({"ROLE_DISTRIBUTOR", "ROLE_ADMIN"})
    public ResponseEntity<?> deliveredOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();

        Optional<Order> optionalOrder = orderRepository.getOrderByOrderNoAndDistributor(id, user);
        Order order = optionalOrder.orElseThrow();
        boolean inTime = false;
        double weight = 0;
        try {
            if (order.getStatus() == OrderStatus.IN_TRANSIT && (order.getDistributor() == user || isAdmin(user))) {
                order.setStatus(OrderStatus.DELIVERED);
                order.setUpdated(LocalDateTime.now());
                orderRepository.save(order);
                inTime = order.getDeliveryDate().isBefore(Instant.ofEpochMilli(order.getDueDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());

                for (OrdersProducts op : order.getOrdersProducts()) {
                    weight += op.getProduct().getWeight();
                }
            }

            if (order.getStatus() != OrderStatus.IN_TRANSIT) {
                return handleWrongData("Wrong status: " + order.getStatus());
            }

            if (order.getDistributor() != user) {
                throw new AccessDeniedException("Transport is taken by another account");
            }

            DeliveredResponse response = new DeliveredResponse(order, inTime, weight);
            order.setOrdersProducts(null);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return handleWrongData(e.getMessage());
        }
    }

    private ResponseEntity<JsonNode> handleWrongData(String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("error", HttpStatus.BAD_REQUEST.value());
        objectNode.put("message", message);
        return ResponseEntity.badRequest().body(objectNode);
    }

    private boolean isAdmin(User user) {
        Optional<Role> optionalRole = roleRepository.findByName(UserRole.ROLE_ADMIN.toString());

        // First check, if we are admin
        return optionalRole.isPresent() && user.getRoles().contains(optionalRole.get());
    }
}
