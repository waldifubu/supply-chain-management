package com.example.supplychainmanagement.entity;

import com.example.supplychainmanagement.model.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    private long articleNo;

    @Column
    private String externalNumber;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "double default 19.99", nullable = false)
    private double price = 19.99;

    @Column(nullable = false)
    private double weight;

    @Transient
    private ProductStatus productStatus = ProductStatus.PENDING;

    @Transient
    private int qty = 1;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int inStock;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Component> componentList;
}