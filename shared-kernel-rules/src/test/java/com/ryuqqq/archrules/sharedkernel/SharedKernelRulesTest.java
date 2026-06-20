package com.ryuqqq.archrules.sharedkernel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.runtime.Runner;
import com.ryuqqq.archrules.sharedkernel.fixture.compliant.shared.kernel.Money;
import com.ryuqqq.archrules.sharedkernel.fixture.violation.orderctx.domain.OrderAggregate;
import com.ryuqqq.archrules.sharedkernel.fixture.violation.shared.kernel.LeakyKernel;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class SharedKernelRulesTest {

    private static final String RULE_KEY = "shared-kernel has no reverse dependency";

    private final ArchRule rule =
            new SharedKernelRules().getRules().get(RULE_KEY).rule();

    @Test
    void ruleIsExposedWithName() {
        assertNotNull(rule, "shared-kernel has no reverse dependency 규칙이 노출되어야 한다");
    }

    @Test
    void compliantMoneyPasses() {
        // Money는 자바 표준만 사용 — 위반 없음
        assertFalse(Runner.check(rule, Money.class).hasViolation(),
                "자바 표준만 의존하는 Money VO는 규칙을 통과해야 한다");
    }

    @Test
    void leakyKernelViolates() {
        // LeakyKernel(shared.kernel)이 OrderAggregate(domain)에 의존 — 위반
        assertTrue(Runner.check(rule, LeakyKernel.class, OrderAggregate.class).hasViolation(),
                "shared.kernel이 domain에 역의존하면 규칙에 위반되어야 한다");
    }
}
