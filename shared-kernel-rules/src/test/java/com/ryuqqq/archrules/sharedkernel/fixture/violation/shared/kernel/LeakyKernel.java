package com.ryuqqq.archrules.sharedkernel.fixture.violation.shared.kernel;

import com.ryuqqq.archrules.sharedkernel.fixture.violation.orderctx.domain.OrderAggregate;

/**
 * shared.kernel이 domain 컨텍스트(orderctx.domain)에 역의존 — 규칙이 잡아야 한다.
 * shared-kernel은 어떤 컨텍스트도 import하지 않아야 하므로 이 클래스는 위반이다.
 */
public class LeakyKernel {

    private final OrderAggregate order;

    public LeakyKernel(OrderAggregate order) {
        this.order = order;
    }

    public String orderId() {
        return order.orderId();
    }
}
