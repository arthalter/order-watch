package com.orderwatch.backend.application.mock;

import com.orderwatch.backend.infrastructure.mock.MockEvidenceRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderEvidenceTools {

    private final MockEvidenceRepository mockEvidenceRepository;

    public OrderEvidenceTools(MockEvidenceRepository mockEvidenceRepository) {
        this.mockEvidenceRepository = mockEvidenceRepository;
    }

    @Tool(description = "根据异常编号查询该异常订单的证据记录。适用于用户询问某个异常为什么发生、有什么证据或依据。")
    public List<OrderEvidenceRecord> queryEvidenceByAnomalyId(
            @ToolParam(description = "异常编号，格式类似 ANOM-001。") String anomalyId
    ) {
        return mockEvidenceRepository.findByAnomalyId(anomalyId);
    }
}
