package com.upb.snack.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "estado_pago", nullable = false, length = 20)
    private String estadoPago;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    public Order() {
    }

    public Order(Long id,
                 User user,
                 Aula aula,
                 String estado,
                 Integer total,
                 String estadoPago,
                 List<OrderItem> items) {
        this.id = id;
        this.user = user;
        this.aula = aula;
        this.estado = estado;
        this.total = total;
        this.estadoPago = estadoPago;
        this.items = items;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (estado == null || estado.isBlank()) {
            estado = "PENDIENTE";
        }
        if (estadoPago == null || estadoPago.isBlank()) {
            estadoPago = "PENDIENTE";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", aula=" + (aula != null ? aula.getId() : null) +
                ", estado='" + estado + '\'' +
                ", total=" + total +
                ", estadoPago='" + estadoPago + '\'' +
                ", createdAt=" + createdAt +
                ", items=" + (items != null ? items.size() : 0) + " items" +
                '}';
    }
}
