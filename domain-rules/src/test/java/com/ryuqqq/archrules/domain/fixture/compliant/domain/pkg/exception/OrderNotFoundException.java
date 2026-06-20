package com.ryuqqq.archrules.domain.fixture.compliant.domain.pkg.exception;

/**
 * ..exception.. 패키지에 위치한 DomainException 서브타입
 * — 규칙 4(exception 위치) compliant.
 */
public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(long orderId) {
        super("Order not found: " + orderId);
    }
}
