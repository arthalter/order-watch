package com.orderwatch.backend.infrastructure.mock;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockEvidenceRepositoryTest {

    private final MockEvidenceRepository repository = new MockEvidenceRepository();

    @Test
    void findsEvidenceByAnomalyId() {
        var evidence = repository.findByAnomalyId("ANOM-001");

        assertThat(evidence).hasSize(3);
        assertThat(evidence)
                .extracting("topic")
                .containsExactly("order-records", "payment-records", "customer-tickets");
        assertThat(evidence.get(0).content()).contains("ORDER-20260427-001");
    }

    @Test
    void returnsEmptyWhenAnomalyIdUnknown() {
        assertThat(repository.findByAnomalyId("ANOM-999")).isEmpty();
        assertThat(repository.findByAnomalyId(" ")).isEmpty();
        assertThat(repository.findByAnomalyId(null)).isEmpty();
    }
}
