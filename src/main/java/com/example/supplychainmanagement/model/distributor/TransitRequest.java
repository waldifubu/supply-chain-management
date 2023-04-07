package com.example.supplychainmanagement.model.distributor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransitRequest {

    private String op;

    @JsonProperty("deliveryDate")
    private String deliveryDateString;
}
