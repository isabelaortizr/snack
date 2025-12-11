package com.upb.snack.controller;

import com.upb.snack.dto.CreateOrderRequest;
import com.upb.snack.dto.UpdateOrderStatusRequest;
import com.upb.snack.entity.Order;
import com.upb.snack.service.OrderService;
import com.upb.snack.service.VerificationCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
// opcional: habilitar CORS para tu frontend (ajusta origen si es necesario)
// @CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderService orderService;
    private final VerificationCodeService verificationCodeService;

    public OrderController(
            OrderService orderService,
            VerificationCodeService verificationCodeService
    ) {
        this.orderService = orderService;
        this.verificationCodeService = verificationCodeService;
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

    // =========================
    //   PREVIEW DE DESCUENTO
    // =========================
    /**
     * Endpoint para que el frontend pueda validar un código de descuento
     * y ver cuánto se descuenta sobre un subtotal dado.
     *
     * Request sencillo:
     *   POST /orders/discount/preview
     *   {
     *     "subtotal": 123,
     *     "code": "ABC123"
     *   }
     */
    @PostMapping("/discount/preview")
    public DiscountPreviewResponse previewDiscount(
            @RequestBody DiscountPreviewRequest request
    ) {
        int subtotal = request.getSubtotal() != null ? request.getSubtotal() : 0;
        String code = request.getCode();

        if (subtotal <= 0) {
            return new DiscountPreviewResponse(
                    false,
                    0,
                    0,
                    subtotal,
                    "El subtotal debe ser mayor a cero."
            );
        }

        if (code == null || code.isBlank()) {
            return new DiscountPreviewResponse(
                    false,
                    0,
                    0,
                    subtotal,
                    "El código es obligatorio."
            );
        }

        boolean valido = verificationCodeService.isValid(code);
        if (!valido) {
            return new DiscountPreviewResponse(
                    false,
                    0,
                    0,
                    subtotal,
                    "Código inválido o expirado."
            );
        }

        int percent = 20; // mismo % que aplicas en OrderService
        int discountAmount = (int) Math.round(subtotal * (percent / 100.0));
        int totalWithDiscount = subtotal - discountAmount;
        if (totalWithDiscount < 0) {
            totalWithDiscount = 0;
        }

        return new DiscountPreviewResponse(
                true,
                percent,
                discountAmount,
                totalWithDiscount,
                "Código aplicado correctamente."
        );
    }

    // =========================
    //   DTOs internos simples
    // =========================

    public static class DiscountPreviewRequest {
        private Integer subtotal;
        private String code;

        public Integer getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(Integer subtotal) {
            this.subtotal = subtotal;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class DiscountPreviewResponse {
        private boolean valid;
        private int percent;
        private int discountAmount;
        private int totalWithDiscount;
        private String message;

        public DiscountPreviewResponse(
                boolean valid,
                int percent,
                int discountAmount,
                int totalWithDiscount,
                String message
        ) {
            this.valid = valid;
            this.percent = percent;
            this.discountAmount = discountAmount;
            this.totalWithDiscount = totalWithDiscount;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        public int getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(int discountAmount) {
            this.discountAmount = discountAmount;
        }

        public int getTotalWithDiscount() {
            return totalWithDiscount;
        }

        public void setTotalWithDiscount(int totalWithDiscount) {
            this.totalWithDiscount = totalWithDiscount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
