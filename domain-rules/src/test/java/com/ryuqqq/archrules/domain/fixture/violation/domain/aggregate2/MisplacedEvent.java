package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate2;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.event.DomainEvent;

/**
 * ..event.. 패키지 밖(..aggregate2..)에 위치한 DomainEvent 구현체
 * — 규칙 3(DomainEvent 구현체는 ..event.. 위치) violation.
 */
public class MisplacedEvent implements DomainEvent {
    private final String payload;

    public MisplacedEvent(String payload) {
        this.payload = payload;
    }

    public String payload() {
        return payload;
    }
}
