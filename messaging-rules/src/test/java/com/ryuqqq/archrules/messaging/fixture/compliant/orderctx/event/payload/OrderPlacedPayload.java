package com.ryuqqq.archrules.messaging.fixture.compliant.orderctx.event.payload;

/**
 * 규칙 통과 픽스처: 순수 DTO — 도메인 애그리거트 의존 없음.
 * 자바 표준 타입만 사용하는 published event payload.
 */
public record OrderPlacedPayload(
        String orderId,
        String customerId,
        long totalAmountCents
) {}
