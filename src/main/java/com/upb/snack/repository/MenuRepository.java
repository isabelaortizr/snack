package com.upb.snack.repository;

import com.upb.snack.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    long countByEstado(String estado);

    @Modifying
    @Transactional
    @Query("update Menu m set m.estado = :estado")
    int actualizarEstadoTodos(String estado);
}

