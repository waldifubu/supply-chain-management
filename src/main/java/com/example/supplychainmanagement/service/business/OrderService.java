package com.example.supplychainmanagement.service.business;

import com.example.supplychainmanagement.dto.CompactOrder;
import com.example.supplychainmanagement.entity.Order;
import com.example.supplychainmanagement.entity.OrdersProducts;
import com.example.supplychainmanagement.entity.Product;
import com.example.supplychainmanagement.entity.Role;
import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.customer.CreateOrderRequest;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.example.supplychainmanagement.model.enums.UserRole;
import com.example.supplychainmanagement.repository.OrderRepository;
import com.example.supplychainmanagement.repository.OrdersProductsRepository;
import com.example.supplychainmanagement.repository.ProductRepository;
import com.example.supplychainmanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersProductsRepository ordersProductsRepository;

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final RoleRepository roleRepository;

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

        boolean inTime = true;
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

    /**
     * Create Order
     *
     * @param request json
     * @param user    given user
     * @return Order object
     */
    public Order createOrder(CreateOrderRequest request, User user) {
        long orderNo = (long) (Math.random() * 1337);
        Order order = new Order(orderNo);
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

        return order;
    }

    public List<Order> getOrderByUserAndStatus(User user, OrderStatus status) {
        return orderRepository.findAllByUserAndStatus(user, status);
    }

    /**
     * Close Order, if possible
     *
     * @param orderNo
     * @param user
     * @return
     */
    public Order closeOrder(long orderNo, User user) {
        Optional<Role> optionalAdminRole = roleRepository.findByName(UserRole.ROLE_ADMIN.toString());
        Optional<Role> optionalEnterpriseRole = roleRepository.findByName(UserRole.ROLE_ENTERPRISE.toString());

        // Check if user has role ADMIN or ENTERPRISE, otherwise it's a customer
        boolean allowedRole = UserRole.contains(optionalAdminRole.map(Role::toString).orElse(null)) ||
                UserRole.contains(optionalEnterpriseRole.map(Role::toString).orElse(null));

        Optional<Order> optionalOrder = allowedRole ? orderRepository.findOrderByOrderNo(orderNo) : orderRepository.findOrderByOrderNoAndUser(orderNo, user);

        Order order = optionalOrder.orElseThrow();

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.REJECTED) {
            order.setUpdated(LocalDateTime.now());
            order.setStatus(OrderStatus.CLOSED);
            order.setUser(user);
            orderRepository.save(order);
        }

        return order;
    }

    public Optional<Order> getOrder(long id) {
        return orderRepository.findOrderByOrderNo(id);
    }

    public Optional<Order> getOrder(long id, User user) {
        return orderRepository.findOrderByOrderNoAndUser(id, user);
    }

    public List<Order> findOrderByStatus(OrderStatus status) {
        return orderRepository.findOrderByStatus(status);
    }

    public Optional<Order> getOrderByOrderNoAndDistributor(long id, User user) {
        return orderRepository.getOrderByOrderNoAndDistributor(id, user);
    }

    public void updateOrder(Order order) {
        order.setUpdated(LocalDateTime.now());
        orderRepository.save(order);
    }
}
