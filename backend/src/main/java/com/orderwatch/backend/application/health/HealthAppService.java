package com.orderwatch.backend.application.health;

import com.orderwatch.backend.domain.health.HealthStatus;
import org.springframework.stereotype.Service;

@Service
public class HealthAppService {

    public HealthResult getHealth() {
        HealthStatus status = HealthStatus.OK;
        return new HealthResult(status.name().toLowerCase());
    }
}
