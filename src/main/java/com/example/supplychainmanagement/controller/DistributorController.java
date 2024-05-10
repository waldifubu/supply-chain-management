package com.example.supplychainmanagement.controller;

import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.OrdersProducts;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.distributor.DeliveredResponse;
import com.example.supplychainmanagement.model.distributor.Delivery;
import com.example.supplychainmanagement.model.distributor.TransitRequest;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.example.supplychainmanagement.repository.UserRepository;
import com.example.supplychainmanagement.service.business.AdminService;
import com.example.supplychainmanagement.service.business.OrderService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/distributor", produces = MediaType.APPLICATION_JSON_VALUE)
public class DistributorController {

    private final UserRepository userRepository;

    private final OrderService orderService;

    private final AdminService adminService;

    private final String UPDATE_DEL_DATE = "update";

    /**
     * Get all orders, which are ready for delivery
     *
     * @return Orders
     */
    @GetMapping("/orders")
    @Secured({"ROLE_DISTRIBUTOR", "ROLE_ADMIN"})
    public ResponseEntity<?> showConveyableOrders() {
        double weight;
        boolean inTime;

        try {
            List<Order> orderList = orderService.findOrderByStatus(OrderStatus.CONVEYABLE);
            List<Delivery> newList = new ArrayList<>();

            for (Order order : orderList) {
                weight = 0.0;
                inTime = false;
                int countProducts = order.getOrdersProducts().size();
                for (OrdersProducts op : order.getOrdersProducts()) {
                    weight += op.getProduct().getWeight();
                }

                order.setOrdersProducts(null);
                if (null != order.getDeliveryDate()) {
                    inTime = LocalDateTime.now().isBefore(order.getDeliveryDate());
                }
                Delivery delivery = new Delivery(
                        order.getOrderNo(),
                        order.getOrderDate(),
                        order.getStatus(),
                        order.getUpdated(),
                        order.getDueDate(),
                        countProducts,
                        inTime,
                        weight
                );

                newList.add(delivery);
            }

            return new ResponseEntity<>(newList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //@todo: split in post for first time with no body and then patch for delivery date change
    /**
     * Distributor sets status order is on the way to customer, for Distributor only
     *
     * @param id
     * @param transitRequest
     * @param authUser
     * @return
     */
    @PatchMapping("/send/{id}")
    @Secured({"ROLE_DISTRIBUTOR"})
    public ResponseEntity<?> transitOrder(
            @PathVariable Long id,
            @RequestBody TransitRequest transitRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
            User user = optionalUser.orElseThrow();

            Optional<Order> optionalOrder = orderService.getOrder(id);
            Order order = optionalOrder.orElseThrow();
            double weight;
            var changed = false;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // First, let's see if it's in correct status. Yes it is
            if (order.getStatus() == OrderStatus.CONVEYABLE) {
                LocalDateTime dateTime = LocalDateTime.parse(transitRequest.getDeliveryDateString(), formatter);
                order.setDeliveryDate(dateTime);
                order.setStatus(OrderStatus.IN_TRANSIT);
                order.setDistributor(user);
                orderService.updateOrder(order);
                changed = true;
            // For corrections, we can use key "modify" in JSON to overwrite the delivery date
            } else if (transitRequest.getModify() != null && transitRequest.getModify().equalsIgnoreCase(UPDATE_DEL_DATE)) {
                LocalDateTime dateTime = LocalDateTime.parse(transitRequest.getDeliveryDateString(), formatter);
                order.setDeliveryDate(dateTime);
                orderService.updateOrder(order);
                changed = true;
            // Otherwise, the order is not in the correct status
            } else {
                handleWrongData("Wrong status of order: " + order.getStatus());
            }

            weight = order.getOrdersProducts().stream().mapToDouble(op -> op.getProduct().getWeight()).sum();
            boolean inTime = order.getDeliveryDate().isBefore(Instant.ofEpochMilli(order.getDueDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            DeliveredResponse response = new DeliveredResponse(order, inTime, weight, changed);
            order.setOrdersProducts(null);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return handleWrongData(e.getMessage());
        }
    }

    /**
     * Order is delivered to customer
     *
     * @param id
     * @param authUser
     * @return
     */
    @PutMapping("/delivered/{id}")
    @Secured({"ROLE_DISTRIBUTOR", "ROLE_ADMIN"})
    public ResponseEntity<?> deliveredOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser
    ) {
        Optional<User> optionalUser = userRepository.findByEmail(authUser.getUsername());
        User user = optionalUser.orElseThrow();

        Optional<Order> optionalOrder = orderService.getOrderByOrderNoAndDistributor(id, user);
        Order order = optionalOrder.orElseThrow();
        boolean inTime = false;
        double weight = 0;
        try {
            if (order.getStatus() == OrderStatus.IN_TRANSIT && (order.getDistributor() == user || adminService.isAdmin(user))) {
                order.setStatus(OrderStatus.DELIVERED);
                orderService.updateOrder(order);

                inTime = order.getDeliveryDate().isBefore(Instant.ofEpochMilli(order.getDueDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                weight = order.getOrdersProducts().stream().mapToDouble(op -> op.getProduct().getWeight()).sum();
            }

            if (order.getStatus() != OrderStatus.IN_TRANSIT) {
                return handleWrongData("Wrong status: " + order.getStatus());
            }

            if (order.getDistributor() != user) {
                throw new AccessDeniedException("Transport is already taken by another account");
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
}
