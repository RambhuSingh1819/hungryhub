package com.fooddelivery.dto;

import lombok.Data;

@Data
public class VerifyPaymentRequest {
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private String appOrderId; // ✅ important
}

