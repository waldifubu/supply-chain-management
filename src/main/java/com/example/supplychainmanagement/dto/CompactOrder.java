package com.example.supplychainmanagement.dto;

import com.example.supplychainmanagement.entity.Product;
import com.example.supplychainmanagement.model.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
public class CompactOrder {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long orderId;

    private Long orderNo;

    private String orderDate;

    private OrderStatus orderStatus;

    private String dueDate;

    //@JsonIgnore
//    @JsonManagedReference
    //@JsonBackReference
    @Convert(converter = CollectionConverter.class)
    private List<Product> products;

    private int amount;

    private LocalDateTime updated;

    private String creatorName;

    private LocalDateTime deliveryDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean inTime;
}
