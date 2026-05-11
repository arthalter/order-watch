package com.orderwatch.backend.infrastructure.mock;

import com.orderwatch.backend.application.mock.OrderAnomaly;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MockOrderDataRepository {

    public List<OrderAnomaly> findRecentAnomalies() {
        return List.of(
                new OrderAnomaly(
                        "ANOM-001",
                        "ORDER-20260427-001",
                        "USER-10086",
                        "PROD-LUXURY-001",
                        "SKU-LUXURY-001",
                        "large_amount_order",
                        "order_amount",
                        "12999",
                        "389",
                        "last_24h",
                        "high",
                        "订单金额 12999 元，显著高于店铺客单价 389 元，建议进入人工审核，不立即发货"
                ),
                new OrderAnomaly(
                        "ANOM-002",
                        "ORDER-20260427-018",
                        "USER-20488",
                        "PROD-COUPON-002",
                        "SKU-COUPON-002",
                        "frequent_cancellation",
                        "cancel_count",
                        "6",
                        "1",
                        "last_24h",
                        "medium",
                        "同一用户 24 小时内下单 8 次，取消 6 次，建议客服确认真实购买意图"
                ),
                new OrderAnomaly(
                        "ANOM-003",
                        "ORDER-20260427-026",
                        "USER-30901",
                        "PROD-PROMO-003",
                        "SKU-PROMO-003",
                        "same_address_multi_account",
                        "new_account_count",
                        "5",
                        "1",
                        "last_24h",
                        "high",
                        "同一收货地址 ADDR-MOCK-009 关联 5 个新用户账号，建议人工审核活动资格"
                )
        );
    }
}
