package com.example.supplychainmanagement.model.distributor;

import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Delivery {

    private long oderNo;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime orderDate;

    private OrderStatus status;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime updated;

    private Date dueDate;

    private int countProducts;

    private boolean inTime;

    private double weight;
}
