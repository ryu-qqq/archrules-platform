package com.ryuqqq.archrules.security.fixture.violation.orderctx.domain;

/**
 * 도메인 애그리게이트 합성 더블.
 * LeakyResponse가 이 타입을 노출하면 규칙이 잡아야 한다.
 */
public class OrderAggregate {
    private final String id;
    private final String status;

    public OrderAggregate(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
}
