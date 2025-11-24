package com.upb.snack.controller;

import com.upb.snack.entity.Menu;
import com.upb.snack.service.MenuService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<Menu> getAllMenuItems() {
        return menuService.getAllMenuItems();
    }

    @GetMapping("/{id}")
    public Menu getMenuItem(@PathVariable Long id) {
        return menuService.getMenuItem(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Menu createMenuItem(@RequestBody Menu menu) {
        return menuService.createMenuItem(menu);
    }

    @PutMapping("/{id}")
    public Menu updateMenuItem(@PathVariable Long id, @RequestBody Menu menu) {
        return menuService.updateMenuItem(id, menu);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
    }
}

