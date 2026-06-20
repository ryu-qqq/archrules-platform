package com.ryuqqq.archrules.persistence.fixture.violation.orderctx.application;

import com.ryuqqq.archrules.persistence.fixture.violation.orderctx.adapter.out.persistence.entity.OrderEntity;

/**
 * application 패키지에서 OrderEntity를 의존하는 서비스 — persistence.entity 밖(application)에서 엔티티 의존하므로 규칙 위반.
 */
public class OrderAppService {
    public String handle(OrderEntity entity) {
        return entity.getStatus();
    }
}
