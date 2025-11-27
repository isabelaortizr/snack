package com.upb.snack.dto;

public class CreateOrderItemRequest {

    private Long menuItemId;
    private Integer cantidad;

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
