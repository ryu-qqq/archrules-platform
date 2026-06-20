package com.ryuqqq.archrules.persistence.fixture.compliant.orderctx.adapter.out.persistence;

import com.ryuqqq.archrules.persistence.fixture.compliant.orderctx.adapter.out.persistence.entity.OrderEntity;

/**
 * persistence 패키지 내부에서 OrderEntity를 의존하는 매퍼 — adapter.out.persistence 패키지에 위치하므로 규칙 통과.
 */
public class OrderMapper {
    public String toStatus(OrderEntity entity) {
        return entity.getStatus();
    }
}
