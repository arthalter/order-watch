package com.orderwatch.backend.interfaces.http;

import com.orderwatch.backend.application.health.HealthAppService;
import com.orderwatch.backend.application.health.HealthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {
    private final HealthAppService healthAppService;

    @GetMapping("/health")
    public HealthResult health() {
        return healthAppService.getHealth();
    }
}
