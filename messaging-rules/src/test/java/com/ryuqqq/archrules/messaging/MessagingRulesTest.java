package com.ryuqqq.archrules.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.messaging.fixture.compliant.orderctx.event.payload.OrderPlacedPayload;
import com.ryuqqq.archrules.messaging.fixture.violation.orderctx.domain.aggregate.OrderAggregate;
import com.ryuqqq.archrules.messaging.fixture.violation.orderctx.event.payload.LeakyPayload;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class MessagingRulesTest {

    private final ArchRule rule =
            new MessagingRules().getRules().get("event payload exposes no aggregate").rule();

    @Test
    void ruleIsExposedWithName() {
        assertNotNull(rule, "EVENT_PAYLOAD_EXPOSES_NO_AGGREGATE 노출");
    }

    @Test
    void compliantPayloadPasses() {
        // OrderPlacedPayload는 순수 DTO — 애그리거트 의존 없음 → 규칙 통과
        assertFalse(Runner.check(rule, OrderPlacedPayload.class).hasViolation());
    }

    @Test
    void leakyPayloadViolates() {
        // LeakyPayload가 OrderAggregate를 직접 참조 → 규칙 위반
        assertTrue(Runner.check(rule, LeakyPayload.class, OrderAggregate.class).hasViolation());
    }
}
