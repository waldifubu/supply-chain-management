package com.example.supplychainmanagement.model.enterprise;

import com.example.supplychainmanagement.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonPropertyOrder({"products", "amount"})
public class ListProducts {

    @JsonProperty("products")
    private List<Product> productList;

    private int amount;
}
