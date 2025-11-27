package com.upb.snack.config;

import com.upb.snack.service.MenuService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MenuScheduler {

    private final MenuService menuService;

    public MenuScheduler(MenuService menuService) {
        this.menuService = menuService;
    }

    // Todos los d??as a las 00:00 (hora Bolivia)
    @Scheduled(cron = "0 0 0 * * *", zone = "America/La_Paz")
    @Transactional
    public void resetMenuDiario() {
        menuService.marcarTodosInactivos();
    }
}
