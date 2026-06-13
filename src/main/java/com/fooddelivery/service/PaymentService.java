package com.fooddelivery.service;

import com.fooddelivery.dto.CreateOrderRequest;
import com.fooddelivery.dto.CreateOrderResponse;
import com.fooddelivery.dto.VerifyPaymentRequest;

public interface PaymentService {
    CreateOrderResponse createRazorpayOrder(CreateOrderRequest req) throws Exception;
    boolean verifyPaymentSignature(VerifyPaymentRequest req);
    void finalizePayment(VerifyPaymentRequest req, boolean isValid);
}
