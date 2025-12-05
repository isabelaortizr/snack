package com.upb.snack.controller;

import com.upb.snack.dto.CreateOrderRequest;
import com.upb.snack.dto.UpdateOrderStatusRequest;
import com.upb.snack.entity.Order;
import com.upb.snack.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
// opcional: habilitar CORS para tu frontend (ajusta origen si es necesario)
// @CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Crear pedido (cliente)
    @PostMapping
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    // Pedido pendiente actual de un usuario (cliente)
    @GetMapping("/current")
    public Order getCurrentOrderForUser(@RequestParam Long userId) {
        return orderService.getCurrentOrderForUser(userId);
    }

    // Cancelar pedido (cliente)
    @PostMapping("/cancel")
    public Order cancelOrder(
            @RequestParam Long orderId,
            @RequestParam Long userId
    ) {
        return orderService.cancelOrder(orderId, userId);
    }

    // Listar todos los pedidos (por si lo necesitas en otra vista admin)
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Listar solo pedidos PENDIENTE (para el panel admin)
    @GetMapping("/pending")
    public List<Order> getPendingOrders() {
        return orderService.getOrdersByEstado("PENDIENTE");
    }

    // Cambiar estado (y opcionalmente estadoPago) de un pedido (panel admin)
    @PatchMapping("/{id}/estado")
    public Order updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateOrderStatus(
                id,
                request.getEstado(),
                request.getEstadoPago()
        );
    }
}
