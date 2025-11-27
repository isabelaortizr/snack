package com.upb.snack.config;

import com.upb.snack.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class OrderAutoCancelScheduler {

    private final OrderService orderService;

    public OrderAutoCancelScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRateString = "60000") // cada 60s
    public void cancelOldPendingOrders() {
        int autoCancelled = orderService.autoCancelOldPendingOrders(30); // 30 min
        // opcional: loguear autoCancelled
    }
}

