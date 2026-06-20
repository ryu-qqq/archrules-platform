package com.ryuqqq.archrules.messaging.fixture.violation.orderctx.event.payload;

import com.ryuqqq.archrules.messaging.fixture.violation.orderctx.domain.aggregate.OrderAggregate;

/**
 * 위반 픽스처: event payload가 도메인 애그리거트를 직접 참조 — 규칙이 잡아야 함.
 * 내부 리팩터(애그리거트 변경) 시 구독자(downstream)가 깨지는 안티패턴.
 */
public class LeakyPayload {
    public OrderAggregate aggregate;
}
