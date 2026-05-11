package com.orderwatch.backend.application.mock;

import com.orderwatch.backend.infrastructure.mock.MockEvidenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderEvidenceTools {

    private final MockEvidenceRepository mockEvidenceRepository;

    public OrderEvidenceTools(MockEvidenceRepository mockEvidenceRepository) {
        this.mockEvidenceRepository = mockEvidenceRepository;
    }

    public List<OrderEvidenceRecord> queryEvidenceByAnomalyId(String anomalyId) {
        return mockEvidenceRepository.findByAnomalyId(anomalyId);
    }
}
