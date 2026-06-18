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
            new HexagonalRules().getRules().get("domain is framework-free").rule();

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

    @Test
    void compliantApplicationPasses() {
        ArchRule appRule = new HexagonalRules().getRules().get("application avoids web/persistence").rule();
        assertFalse(Runner.check(appRule,
                com.ryuqqq.archrules.hexagonal.fixture.compliant.application.OrderAppService.class).hasViolation());
    }

    @Test
    void leakyApplicationViolates() {
        ArchRule appRule = new HexagonalRules().getRules().get("application avoids web/persistence").rule();
        assertTrue(Runner.check(appRule,
                com.ryuqqq.archrules.hexagonal.fixture.violation.application.LeakyAppService.class).hasViolation());
    }
}
