package com.example.supplychainmanagement.model.enterprise;

import com.example.supplychainmanagement.model.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RequestPartsOrderResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID id;

    private String articleCode;

    private int qty;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String comment;

    private RequestStatus status;

    private LocalDateTime created;
}
