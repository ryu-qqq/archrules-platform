package com.ryuqqq.archrules.security.fixture.violation.orderctx.adapter.in.publicapi;

import com.ryuqqq.archrules.security.fixture.violation.orderctx.domain.OrderAggregate;

/**
 * public 입력 어댑터 DTO가 도메인 타입을 직접 노출 — 규칙이 잡아야 함.
 * 도메인 애그리게이트를 API 응답에 그대로 노출하는 안티패턴.
 */
public class LeakyResponse {
    private final OrderAggregate order;

    public LeakyResponse(OrderAggregate order) {
        this.order = order;
    }

    public OrderAggregate getOrder() { return order; }
}
