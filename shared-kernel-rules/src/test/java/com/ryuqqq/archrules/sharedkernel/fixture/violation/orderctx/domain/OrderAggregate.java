package com.ryuqqq.archrules.sharedkernel.fixture.violation.orderctx.domain;

/** 주문 컨텍스트 도메인 객체 — violation 시나리오에서 shared.kernel이 이것을 의존한다. */
public class OrderAggregate {

    private final String orderId;

    public OrderAggregate(String orderId) {
        this.orderId = orderId;
    }

    public String orderId() {
        return orderId;
    }
}
