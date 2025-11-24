package com.upb.snack.service;

import com.upb.snack.entity.Order;
import com.upb.snack.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        Order safeOrder = Objects.requireNonNull(order, "La orden es obligatoria");
        return orderRepository.save(safeOrder);
    }
}