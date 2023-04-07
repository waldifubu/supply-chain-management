package com.example.supplychainmanagement.entity;

import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.enums.StorageStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "storage")
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int inStock = 0;

    private Date updated;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private StorageStatus storageStatus = StorageStatus.NOT_REQUESTED;

    private Date lastDelivery;

    private Date nextDelivery;

    public Storage(Long id, Product p, int inStock, Date updated, Date lastDelivery, Date nextDelivery) {
        this.id = id;
        product = p;
        this.inStock = inStock;
        this.updated = updated;
        this.lastDelivery = lastDelivery;
        this.nextDelivery = nextDelivery;
    }
}
