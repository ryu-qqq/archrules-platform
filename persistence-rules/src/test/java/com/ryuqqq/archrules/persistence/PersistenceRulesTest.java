package com.ryuqqq.archrules.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class PersistenceRulesTest {

    private final ArchRule rule =
            new PersistenceRules().getRules().get("jpa entity does not leak outside persistence").rule();

    @Test
    void ruleIsExposedWithName() {
        assertNotNull(rule, "JPA_ENTITY_DOES_NOT_LEAK 노출");
    }

    @Test
    void compliantEntityWithInternalMapper_passes() {
        // mapper가 persistence 내부에 있으므로 엔티티 의존이 허용된다.
        assertFalse(
                Runner.check(
                        rule,
                        compliant.orderctx.adapter.out.persistence.entity.OrderEntity.class,
                        compliant.orderctx.adapter.out.persistence.OrderMapper.class)
                        .hasViolation(),
                "persistence 내부 의존은 통과해야 한다");
    }

    @Test
    void violationEntityWithApplicationService_detected() {
        // application 패키지의 OrderAppService가 엔티티를 의존하므로 위반이다.
        assertTrue(
                Runner.check(
                        rule,
                        violation.orderctx.adapter.out.persistence.entity.OrderEntity.class,
                        violation.orderctx.application.OrderAppService.class)
                        .hasViolation(),
                "application 패키지에서 엔티티 의존은 위반이어야 한다");
    }
}
