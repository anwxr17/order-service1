package com.ecommerce.orderservice1.controller;

import com.ecommerce.orderservice1.entity.Order;
import com.ecommerce.orderservice1.entity.OrderItem;
import com.ecommerce.orderservice1.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    
    // NEW: Root endpoint for http://localhost:8083
    @GetMapping("/")
    public String home() {
        return "🚀 OrderService1 is running! Available endpoints:<br>" +
               "• GET /api/orders/ - This page<br>" +
               "• POST /api/orders - Create order<br>" +
               "• GET /api/orders/{id} - Get order by ID<br>" +
               "• GET /api/orders/user/{userId} - Get user's orders<br>" +
               "• POST /api/orders/{id}/pay - Mark order as PAID<br>" +
               "• POST /api/orders/{id}/cancel - Mark order as CANCELLED<br>" +
               "• H2 Console: <a href='/h2-console'>/h2-console</a>";
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