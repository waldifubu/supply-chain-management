package com.example.supplychainmanagement.entity;

import com.example.supplychainmanagement.model.enums.StorageStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "storage")
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Product product;

    private int stock = 0;

    private LocalDateTime updated;

    @Enumerated(EnumType.STRING)
    private StorageStatus storageStatus = StorageStatus.NOT_REQUESTED;

    @Transient
    private String productName;

    public Storage(Long id, Product p, int stock, LocalDateTime updated) {
        this.id = id;
        product = p;
        this.stock = stock;
        this.updated = updated;
    }
}
