package com.upb.snack.service;

import com.upb.snack.entity.OrderItem;
import com.upb.snack.repository.OrderItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    public OrderItem getOrderItem(Long id) {
        Long safeId = requireId(id);
        return orderItemRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Item de orden no encontrado: " + id));
    }

    public OrderItem createOrderItem(OrderItem orderItem) {
        OrderItem safeItem = Objects.requireNonNull(orderItem, "El item es obligatorio");
        return orderItemRepository.save(safeItem);
    }

    public OrderItem updateOrderItem(Long id, OrderItem orderItem) {
        Long safeId = requireId(id);
        OrderItem safeItem = Objects.requireNonNull(orderItem, "El item es obligatorio");
        if (!orderItemRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Item de orden no encontrado: " + id);
        }
        return orderItemRepository.save(safeItem);
    }

    public void deleteOrderItem(Long id) {
        Long safeId = requireId(id);
        if (!orderItemRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Item de orden no encontrado: " + id);
        }
        orderItemRepository.deleteById(safeId);
    }

    private Long requireId(Long id) {
        return Objects.requireNonNull(id, "El id del item es obligatorio");
    }
}


