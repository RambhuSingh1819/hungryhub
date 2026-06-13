package com.fooddelivery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Payment;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.AdminRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.fooddelivery.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.url=jdbc:h2:mem:fooddeliverytest;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "razorpay.key.id=rzp_test_dummy",
    "razorpay.key.secret=dummypassword"
})
@AutoConfigureMockMvc
public class PaymentIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCustomerPaymentFlow() throws Exception {
        // 1. Get the seeded test user
        User user = userRepository.findByEmail("user@hungryhub.com")
                .orElseThrow(() -> new AssertionError("Seeded user not found"));

        // Create a session for the user
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user);

        // 2. Fetch a food item to add to the cart
        FoodItem foodItem = foodItemRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("No food items seeded"));

        // 3. Add the item to the cart via UserController endpoint
        mockMvc.perform(post("/user/cart/add")
                .param("foodId", String.valueOf(foodItem.getId()))
                .session(session))
                .andExpect(status().is3xxRedirection());

        // 4. Create the order from the cart
        Map<String, String> orderParams = new HashMap<>();
        orderParams.put("deliveryAddress", "123 Hunger Road, Foodville");
        orderParams.put("specialInstructions", "Make it spicy");

        MvcResult orderResult = mockMvc.perform(post("/user/orders/create-from-cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderParams))
                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String orderResponseContent = orderResult.getResponse().getContentAsString();
        Map<String, Object> orderResponse = objectMapper.readValue(orderResponseContent, Map.class);
        
        assertTrue((Boolean) orderResponse.get("success"));
        String appOrderId = (String) orderResponse.get("orderId");
        assertNotNull(appOrderId);

        // Verify that the order was created in DB with PENDING status
        Order order = orderRepository.findByOrderId(appOrderId)
                .orElseThrow(() -> new AssertionError("Order not saved in database"));
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
        assertEquals(Order.PaymentStatus.PENDING, order.getPaymentStatus());

        // 5. Initiate Razorpay order creation
        Map<String, Object> paymentOrderParams = new HashMap<>();
        paymentOrderParams.put("amount", order.getTotalAmount());
        paymentOrderParams.put("appOrderId", appOrderId);

        MvcResult paymentOrderResult = mockMvc.perform(post("/payment/create-order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentOrderParams))
                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String paymentOrderResponseContent = paymentOrderResult.getResponse().getContentAsString();
        Map<String, Object> paymentOrderResponse = objectMapper.readValue(paymentOrderResponseContent, Map.class);

        String razorpayOrderId = (String) paymentOrderResponse.get("razorpayOrderId");
        assertNotNull(razorpayOrderId);
        assertTrue(razorpayOrderId.startsWith("rzp_mock_"));

        // Verify order payment status is set to PROCESSING in DB
        order = orderRepository.findByOrderId(appOrderId).get();
        assertEquals(Order.PaymentStatus.PROCESSING, order.getPaymentStatus());
        assertEquals(razorpayOrderId, order.getRazorpayOrderId());

        // 6. Simulate Razorpay webhook / success verification
        Map<String, String> verifyParams = new HashMap<>();
        verifyParams.put("razorpayPaymentId", "pay_test_cust_12345");
        verifyParams.put("razorpayOrderId", razorpayOrderId);
        verifyParams.put("razorpaySignature", "sig_test_cust_12345");
        verifyParams.put("appOrderId", appOrderId);

        MvcResult verifyResult = mockMvc.perform(post("/payment/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyParams))
                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String verifyResponseContent = verifyResult.getResponse().getContentAsString();
        Map<String, Object> verifyResponse = objectMapper.readValue(verifyResponseContent, Map.class);
        assertTrue((Boolean) verifyResponse.get("success"));

        // Verify in database that order is CONFIRMED and payment is COMPLETED
        order = orderRepository.findByOrderId(appOrderId).get();
        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(Order.PaymentStatus.COMPLETED, order.getPaymentStatus());
        assertEquals("pay_test_cust_12345", order.getRazorpayPaymentId());
        assertEquals("sig_test_cust_12345", order.getRazorpaySignature());

        // Verify Payment record is saved
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new AssertionError("Payment record not found"));
        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals("pay_test_cust_12345", payment.getTransactionId());
        assertEquals(Payment.PaymentMethod.RAZORPAY, payment.getPaymentMethod());
    }

    @Test
    public void testAdminSubscriptionFlow() throws Exception {
        // 1. Get the seeded test admin
        Admin admin = adminRepository.findByEmail("admin@hungryhub.com")
                .orElseThrow(() -> new AssertionError("Seeded admin not found"));

        // Set admin to unpaid initially to test the transition
        admin.setPaid(false);
        admin.setPlanType(null);
        admin.setSubscriptionExpiry(null);
        adminRepository.save(admin);

        // Create a session for the admin
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", admin);

        // 2. Initiate admin subscription order creation
        MvcResult subOrderResult = mockMvc.perform(post("/payment/admin/create-subscription-order")
                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String subOrderResponseContent = subOrderResult.getResponse().getContentAsString();
        Map<String, Object> subOrderResponse = objectMapper.readValue(subOrderResponseContent, Map.class);

        assertTrue((Boolean) subOrderResponse.get("success"));
        String razorpayOrderId = (String) subOrderResponse.get("razorpayOrderId");
        String appOrderId = (String) subOrderResponse.get("appOrderId");
        assertNotNull(razorpayOrderId);
        assertNotNull(appOrderId);
        assertTrue(razorpayOrderId.startsWith("rzp_mock_"));
        assertTrue(appOrderId.startsWith("ADMIN_SUB_"));

        // 3. Verify payment signature and complete subscription activation
        Map<String, String> verifyParams = new HashMap<>();
        verifyParams.put("razorpayPaymentId", "pay_test_admin_sub_999");
        verifyParams.put("razorpayOrderId", razorpayOrderId);
        verifyParams.put("razorpaySignature", "sig_test_admin_sub_999");
        verifyParams.put("appOrderId", appOrderId);

        MvcResult verifyResult = mockMvc.perform(post("/payment/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyParams))
                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String verifyResponseContent = verifyResult.getResponse().getContentAsString();
        Map<String, Object> verifyResponse = objectMapper.readValue(verifyResponseContent, Map.class);
        assertTrue((Boolean) verifyResponse.get("success"));

        // 4. Verify admin status in DB is active and marked paid
        Admin updatedAdmin = adminRepository.findById(admin.getId()).get();
        assertTrue(updatedAdmin.isPaid());
        assertEquals("MONTHLY", updatedAdmin.getPlanType());
        assertNotNull(updatedAdmin.getSubscriptionExpiry());
        assertTrue(updatedAdmin.getSubscriptionExpiry().isAfter(LocalDate.now()));
    }
}
