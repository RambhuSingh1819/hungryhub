package com.fooddelivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateOrderResponse {

	    private String razorpayKeyId;
	    private String razorpayOrderId;
	    private Long amountInPaise;
	    private String currency;
	    private String appOrderId; // ✅ add this if missing

}
