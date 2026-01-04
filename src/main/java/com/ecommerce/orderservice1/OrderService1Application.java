package com.ecommerce.orderservice1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderService1Application {
    public static void main(String[] args) {
        SpringApplication.run(OrderService1Application.class, args);
        System.out.println("✅ OrderService1 is running on http://localhost:8083");
    }
}