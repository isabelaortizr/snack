package com.upb.snack.repository;

import com.upb.snack.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Para bloquear nuevos pedidos si hay un PENDIENTE
    Optional<Order> findFirstByUser_IdAndEstadoOrderByCreatedAtDesc(Long userId, String estado);

    // Para buscar pendientes viejos (para auto-cancelar)
    List<Order> findByEstadoAndCreatedAtBefore(String estado, LocalDateTime limite);
}
