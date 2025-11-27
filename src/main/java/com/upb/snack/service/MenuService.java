package com.upb.snack.service;

import com.upb.snack.entity.Menu;
import com.upb.snack.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> getAllMenuItems() {
        return menuRepository.findAll();
    }

    public Menu getMenuItem(Long id) {
        Long safeId = requireId(id);
        return menuRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Producto de men?? no encontrado: " + id));
    }

    public Menu createMenuItem(Menu menu) {
        Menu safeMenu = Objects.requireNonNull(menu, "El producto es obligatorio");

        // Aseguramos que el stock nunca sea null
        if (safeMenu.getStock() == null) {
            safeMenu.setStock(0);
        }

        // Si no viene estado, por defecto INACTIVO (o ACTIVO, como prefieras)
        if (safeMenu.getEstado() == null || safeMenu.getEstado().isBlank()) {
            safeMenu.setEstado("INACTIVO");
        }

        return menuRepository.save(safeMenu);
    }

    public Menu updateMenuItem(Long id, Menu menu) {
        Long safeId = requireId(id);
        Menu safeMenu = Objects.requireNonNull(menu, "El producto es obligatorio");

        Menu existing = menuRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Producto de men?? no encontrado: " + id));

        // Copiamos campo por campo, incluyendo stock
        existing.setNombreProducto(safeMenu.getNombreProducto());
        existing.setPrecio(safeMenu.getPrecio());
        existing.setDescripcion(safeMenu.getDescripcion());
        existing.setImageUrl(safeMenu.getImageUrl());
        existing.setEstado(safeMenu.getEstado());

        if (safeMenu.getStock() != null) {
            existing.setStock(safeMenu.getStock());
        } else if (existing.getStock() == null) {
            existing.setStock(0);
        }

        return menuRepository.save(existing);
    }

    public void deleteMenuItem(Long id) {
        Long safeId = requireId(id);
        if (!menuRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Producto de men?? no encontrado: " + id);
        }
        menuRepository.deleteById(safeId);
    }

    private Long requireId(Long id) {
        return Objects.requireNonNull(id, "El id del producto es obligatorio");
    }

    // ================== L??GICA DE ESTADO ==================

    /**
     * Marca TODOS los productos del men?? como INACTIVO.
     * Lo usa el scheduler diario a las 00:00.
     */
    @Transactional
    public void marcarTodosInactivos() {
        menuRepository.actualizarEstadoTodos("INACTIVO");
    }

    /**
     * Cambia el estado de un producto individual (check en el front).
     */
    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Long safeId = requireId(id);
        Menu menu = menuRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Producto de men?? no encontrado: " + id));

        menu.setEstado(activo ? "ACTIVO" : "INACTIVO");
        menuRepository.save(menu);
    }

    /**
     * Devuelve true si NO hay ning??n producto ACTIVO.
     * Lo usas para decidir si mostrar el modal "Actualiza el men?? del d??a".
     */
    public boolean todosInactivos() {
        return menuRepository.countByEstado("ACTIVO") == 0;
    }
}

