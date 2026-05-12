package com.orderwatch.backend.application.mock;

import com.orderwatch.backend.infrastructure.mock.MockOrderDataRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderMetricsTools {

    private final MockOrderDataRepository mockOrderDataRepository;

    public OrderMetricsTools(MockOrderDataRepository mockOrderDataRepository) {
        this.mockOrderDataRepository = mockOrderDataRepository;
    }

    @Tool(description = "查询最近24小时内发现的异常订单列表，返回异常编号、订单号、异常类型、指标、风险等级和摘要。")
    public List<OrderAnomaly> queryOrderAnomalies() {
        return mockOrderDataRepository.findRecentAnomalies();
    }
}
