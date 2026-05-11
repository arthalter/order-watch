package com.orderwatch.backend.infrastructure.mock;

import com.orderwatch.backend.application.mock.OrderEvidenceRecord;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MockEvidenceRepository {

    private static final List<OrderEvidenceRecord> EVIDENCE = List.of(
            new OrderEvidenceRecord(
                    "EV-001",
                    "ANOM-001",
                    "order-records",
                    "订单 ORDER-20260427-001 金额 12999 元，显著高于店铺客单价 389 元"
            ),
            new OrderEvidenceRecord(
                    "EV-002",
                    "ANOM-001",
                    "payment-records",
                    "订单 ORDER-20260427-001 已支付成功，支付金额 12999 元"
            ),
            new OrderEvidenceRecord(
                    "EV-003",
                    "ANOM-001",
                    "customer-tickets",
                    "客服备注：用户要求尽快发货，建议发货前人工复核"
            ),
            new OrderEvidenceRecord(
                    "EV-004",
                    "ANOM-002",
                    "order-records",
                    "用户 USER-20488 在 24 小时内下单 8 次，其中 6 次取消"
            ),
            new OrderEvidenceRecord(
                    "EV-005",
                    "ANOM-002",
                    "customer-tickets",
                    "客服记录：用户多次咨询优惠券是否可重复使用"
            ),
            new OrderEvidenceRecord(
                    "EV-006",
                    "ANOM-003",
                    "order-records",
                    "收货地址 ADDR-MOCK-009 关联 5 个新用户账号"
            ),
            new OrderEvidenceRecord(
                    "EV-007",
                    "ANOM-003",
                    "customer-tickets",
                    "客服记录：多个账号咨询同一活动商品的发货时间"
            )
    );

    public List<OrderEvidenceRecord> findByAnomalyId(String anomalyId) {
        if (anomalyId == null || anomalyId.isBlank()) {
            return List.of();
        }

        return EVIDENCE.stream()
                .filter(record -> anomalyId.equals(record.anomalyId()))
                .toList();
    }
}
