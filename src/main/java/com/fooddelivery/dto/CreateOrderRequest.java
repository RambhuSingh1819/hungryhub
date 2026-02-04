package com.fooddelivery.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private BigDecimal amount;
    private String appOrderId;
}
