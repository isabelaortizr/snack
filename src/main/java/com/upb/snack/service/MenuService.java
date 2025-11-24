package com.upb.snack.service;

import com.upb.snack.entity.Menu;
import com.upb.snack.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new EntityNotFoundException("Producto de menú no encontrado: " + id));
    }

    public Menu createMenuItem(Menu menu) {
        Menu safeMenu = Objects.requireNonNull(menu, "El producto es obligatorio");
        return menuRepository.save(safeMenu);
    }

    public Menu updateMenuItem(Long id, Menu menu) {
        Long safeId = requireId(id);
        Menu safeMenu = Objects.requireNonNull(menu, "El producto es obligatorio");
        if (!menuRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Producto de menú no encontrado: " + id);
        }
        return menuRepository.save(safeMenu);
    }

    public void deleteMenuItem(Long id) {
        Long safeId = requireId(id);
        if (!menuRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Producto de menú no encontrado: " + id);
        }
        menuRepository.deleteById(safeId);
    }

    private Long requireId(Long id) {
        return Objects.requireNonNull(id, "El id del producto es obligatorio");
    }
}

