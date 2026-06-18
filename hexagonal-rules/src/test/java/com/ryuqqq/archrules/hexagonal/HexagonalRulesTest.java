package com.ryuqqq.archrules.hexagonal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.hexagonal.fixture.compliant.domain.OrderId;
import com.ryuqqq.archrules.hexagonal.fixture.violation.domain.SpringCoupledDomain;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class HexagonalRulesTest {

    private final ArchRule rule =
            new HexagonalRules().getRules().get("domain is framework-free");

    @Test
    void ruleIsExposedWithName() {
        assertNotNull(rule, "DOMAIN_FRAMEWORK_FREE 노출");
    }

    @Test
    void compliantDomainPasses() {
        assertFalse(Runner.check(rule, OrderId.class).hasViolation());
    }

    @Test
    void frameworkCoupledDomainViolates() {
        assertTrue(Runner.check(rule, SpringCoupledDomain.class).hasViolation());
    }
}
