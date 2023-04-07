package com.example.supplychainmanagement.model.enterprise;

import com.example.supplychainmanagement.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListProducts {

    private List<Product> productList;

    private int amount;
}
