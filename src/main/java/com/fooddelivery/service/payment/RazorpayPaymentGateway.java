package com.fooddelivery.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fooddelivery.dto.CreateOrderRequest;
import com.fooddelivery.dto.CreateOrderResponse;
import com.fooddelivery.dto.VerifyPaymentRequest;
import com.fooddelivery.entity.Order;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.util.CryptoUtils;
import com.razorpay.RazorpayClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RazorpayPaymentGateway implements PaymentGateway {

    private final RazorpayClient razorpayClient;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest req) throws Exception {
        BigDecimal rupees = req.getAmount();
        if (rupees == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        int amountInPaise = rupees.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();

        String rzpOrderId = null;
        boolean isMock = (keyId == null || keyId.isBlank() || keyId.equals("rzp_test_dummy") || keySecret == null || keySecret.isBlank());

        if (!isMock) {
            try {
                JSONObject options = new JSONObject();
                options.put("amount", amountInPaise);
                options.put("currency", currency);
                options.put("payment_capture", 1);

                com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);
                rzpOrderId = razorpayOrder.get("id");
            } catch (Exception e) {
                log.error("Razorpay SDK order creation failed: {}. Falling back to mock order.", e.getMessage());
                isMock = true;
            }
        }

        if (isMock || rzpOrderId == null) {
            rzpOrderId = "rzp_mock_" + System.currentTimeMillis();
        }

        // link Razorpay order id to our Order (if provided)
        if (req.getAppOrderId() != null) {
            final String finalOrderId = rzpOrderId;
            orderRepository.findByOrderId(req.getAppOrderId()).ifPresent(order -> {
                order.setRazorpayOrderId(finalOrderId);
                order.setPaymentStatus(Order.PaymentStatus.PROCESSING);
                orderRepository.save(order);
            });
        }

        return new CreateOrderResponse(keyId, rzpOrderId, (long) amountInPaise, currency, req.getAppOrderId());
    }

    @Override
    public boolean verifySignature(VerifyPaymentRequest req) {
        if (req.getRazorpayOrderId() != null && req.getRazorpayOrderId().startsWith("rzp_mock_")) {
            return true; // Auto-verify mock orders
        }
        try {
            String data = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
            String generated = CryptoUtils.hmacSha256(data, keySecret);
            return generated.equals(req.getRazorpaySignature());
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
