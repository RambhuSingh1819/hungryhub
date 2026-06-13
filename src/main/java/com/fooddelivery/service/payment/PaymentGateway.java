package com.fooddelivery.service.payment;

import com.fooddelivery.dto.CreateOrderRequest;
import com.fooddelivery.dto.CreateOrderResponse;
import com.fooddelivery.dto.VerifyPaymentRequest;

public interface PaymentGateway {
    CreateOrderResponse createOrder(CreateOrderRequest request) throws Exception;
    boolean verifySignature(VerifyPaymentRequest request);
}
