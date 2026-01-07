package com.ecommerce.orderservice1.controller;

import com.ecommerce.orderservice1.entity.Order;
import com.ecommerce.orderservice1.entity.OrderItem;
import com.ecommerce.orderservice1.service.OrderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    
    @Value("${stripe.public.key}")
    private String stripePublicKey;
    
    // Initialize Stripe API
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
    
    // Root endpoint
    @GetMapping("/")
    public String home() {
        return "🚀 OrderService1 is running! Available endpoints:<br>" +
               "• GET /api/orders/ - This page<br>" +
               "• POST /api/orders - Create order<br>" +
               "• GET /api/orders/{id} - Get order by ID<br>" +
               "• GET /api/orders/user/{userId} - Get user's orders<br>" +
               "• POST /api/orders/{id}/pay - Mark order as PAID<br>" +
               "• POST /api/orders/{id}/cancel - Mark order as CANCELLED<br>" +
               "• POST /api/orders/payment-intent - Create Stripe PaymentIntent<br>" +
               "• GET /api/orders/test-keys - Verify Stripe keys";
    }
    
    // Create Stripe PaymentIntent
    @PostMapping("/payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody PaymentIntentRequest request) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", request.getAmount());
            params.put("currency", request.getCurrency());
            params.put("automatic_payment_methods", Map.of("enabled", true));
            
            if (request.getOrderId() != null) {
                params.put("metadata", Map.of("orderId", request.getOrderId()));
            }
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("publishableKey", stripePublicKey);
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Test endpoint to verify Stripe keys
    @GetMapping("/test-keys")
    public Map<String, String> testStripeKeys() {
        Map<String, String> keys = new HashMap<>();
        keys.put("publicKey", stripePublicKey != null ? stripePublicKey.substring(0, 20) + "..." : "NULL");
        keys.put("secretKeyLoaded", stripeSecretKey != null ? "YES" : "NO");
        keys.put("message", "Stripe is configured!");
        return keys;
    }
    
    @PostMapping
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getAddress());
        order.setItems(request.getItems());
        return orderService.createOrder(order);
    }
    
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderService.getOrder(id);
    }
    
    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable String userId) {
        return orderService.getOrdersByUser(userId);
    }
    
    @PostMapping("/{id}/pay")
    public Order payOrder(@PathVariable String id) {
        return orderService.updateStatus(id, "PAID");
    }
    
    @PostMapping("/{id}/cancel")
    public Order cancelOrder(@PathVariable String id) {
        return orderService.updateStatus(id, "CANCELLED");
    }
}

@Data
class CreateOrderRequest {
    private String userId;
    private String address;
    private List<OrderItem> items;
}

@Data
class PaymentIntentRequest {
    private Long amount;    // in cents (e.g., 2000 = $20.00)
    private String currency = "usd";
    private String orderId; // optional: your internal order ID
}