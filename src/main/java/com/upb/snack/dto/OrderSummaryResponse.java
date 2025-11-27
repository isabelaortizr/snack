package com.upb.snack.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSummaryResponse {

    private Long id;
    private String estado;          // PENDIENTE, CONFIRMADO, CANCELADO...
    private String estadoPago;
    private Integer total;
    private String aula;           // "Edificio 1 - Piso 2 - B3"
    private LocalDateTime createdAt;
    private List<OrderItemSummary> items;

    // getters/setters

    public static class OrderItemSummary {
        private Long menuItemId;
        private String nombreProducto;
        private Integer cantidad;
        private Integer precioItem;
        private Integer subtotal;
        // getters/setters
    }
}
