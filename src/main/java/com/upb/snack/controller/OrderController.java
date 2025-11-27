package com.upb.snack.controller;

import com.upb.snack.dto.CreateOrderRequest;
import com.upb.snack.entity.Order;
import com.upb.snack.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/current")
    public Order getCurrentOrderForUser(@RequestParam Long userId) {
        return orderService.getCurrentOrderForUser(userId);
    }

    @PostMapping("/cancel")
    public Order cancelOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId
    ) {
        return orderService.cancelOrder(orderId, userId);
    }
}
