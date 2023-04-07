package com.example.supplychainmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "components")
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Product product;

    private String manufacturer;

    private String name;

    private String articleNo;

    public Component(Product product, String manufacturer, String name, String articleNo) {
        this.product = product;
        this.manufacturer = manufacturer;
        this.name = name;
        this.articleNo = articleNo;
    }
}
