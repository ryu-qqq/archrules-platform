package com.ryuqqq.archrules.messaging.fixture.violation.orderctx.domain.aggregate;

/**
 * 위반 픽스처: 도메인 애그리거트 — LeakyPayload가 이 클래스를 참조해 규칙을 위반시킨다.
 */
public class OrderAggregate {
    private String orderId;
    private long totalAmountCents;

    public String getOrderId() { return orderId; }
    public long getTotalAmountCents() { return totalAmountCents; }
}
