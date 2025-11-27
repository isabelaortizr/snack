package com.upb.snack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private Menu menuItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore // 👈 evita la recursión Order -> items -> order -> ...
    private Order order;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_item", nullable = false)
    private Integer precioItem;

    @Column(name = "subtotal", nullable = false)
    private Integer subtotal;

    public OrderItem() {
    }

    public OrderItem(Long id,
                     Menu menuItem,
                     Order order,
                     Integer cantidad,
                     Integer precioItem,
                     Integer subtotal) {
        this.id = id;
        this.menuItem = menuItem;
        this.order = order;
        this.cantidad = cantidad;
        this.precioItem = precioItem;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Menu getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(Menu menuItem) {
        this.menuItem = menuItem;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getPrecioItem() {
        return precioItem;
    }

    public void setPrecioItem(Integer precioItem) {
        this.precioItem = precioItem;
    }

    public Integer getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Integer subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", menuItem=" + (menuItem != null ? menuItem.getId() : null) +
                ", order=" + (order != null ? order.getId() : null) +
                ", cantidad=" + cantidad +
                ", precioItem=" + precioItem +
                ", subtotal=" + subtotal +
                '}';
    }
}
