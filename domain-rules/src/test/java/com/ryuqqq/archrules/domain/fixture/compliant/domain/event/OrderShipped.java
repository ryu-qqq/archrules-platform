package com.ryuqqq.archrules.domain.fixture.compliant.domain.event;

/** ..event.. 패키지에 위치한 DomainEvent 구현체 — 규칙 3(event 위치) compliant. */
public class OrderShipped implements DomainEvent {
    private final long orderId;

    public OrderShipped(long orderId) {
        this.orderId = orderId;
    }

    public long orderId() {
        return orderId;
    }
}
