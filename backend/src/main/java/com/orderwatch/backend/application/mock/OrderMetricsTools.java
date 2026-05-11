package com.orderwatch.backend.application.mock;

import com.orderwatch.backend.infrastructure.mock.MockOrderDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderMetricsTools {

    private final MockOrderDataRepository mockOrderDataRepository;

    public OrderMetricsTools(MockOrderDataRepository mockOrderDataRepository) {
        this.mockOrderDataRepository = mockOrderDataRepository;
    }

    public List<OrderAnomaly> queryOrderAnomalies() {
        return mockOrderDataRepository.findRecentAnomalies();
    }
}
