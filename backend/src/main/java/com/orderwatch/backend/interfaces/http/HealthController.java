package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.application.health.HealthAppService;
import com.orderwatch.backend.application.health.HealthResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final HealthAppService healthAppService;

    public HealthController(HealthAppService healthAppService) {
        this.healthAppService = healthAppService;
    }

    @GetMapping("/health")
    public HealthResult health() {
        return healthAppService.getHealth();
    }
}
