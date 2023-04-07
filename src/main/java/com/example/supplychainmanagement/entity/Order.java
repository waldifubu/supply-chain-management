package com.example.supplychainmanagement.entity;

import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@ToString(exclude = {"orderDate", "updated"})
@JsonIgnoreProperties({"creator", "id"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long orderNo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<OrdersProducts> ordersProducts;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy hh:mm")
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name = "order_date", columnDefinition = "TIMESTAMP")
//    @JsonSerialize(using = LocalDateSerializer.class)
//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime orderDate;

    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.CREATED;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "customer_id")
    private User user;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime deliveryDate;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "distributor_id")
    private User distributor;

    public Order(Long id, long orderNo, LocalDateTime orderDate, OrderStatus orderStatus, Date dueDate, LocalDateTime updated, LocalDateTime deliveryDate) {
        this.id = id;
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.dueDate = dueDate;
        this.updated = updated;
        this.deliveryDate = deliveryDate;
    }

    public Order(long orderNo) {
        this.orderNo = orderNo;
    }
}
