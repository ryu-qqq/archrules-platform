package com.ryuqqq.archrules.hexagonal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.hexagonal.fixture.compliant.domain.InstantDomain;
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

    @Test
    void layerDirectionViolationIsCaught() {
        ArchRule layerRule = new HexagonalRules().getRules().get("hexagonal layer direction").rule();
        assertTrue(Runner.check(layerRule,
                com.ryuqqq.archrules.hexagonal.fixture.violation.layers.domain.LeakyLayerDomain.class,
                com.ryuqqq.archrules.hexagonal.fixture.violation.layers.adapter.out.OutboundAdapter.class)
                .hasViolation(), "domain→adapter.out 는 레이어 위반");
    }

    @Test
    void correctLayerDirectionPasses() {
        ArchRule layerRule = new HexagonalRules().getRules().get("hexagonal layer direction").rule();
        assertFalse(Runner.check(layerRule,
                com.ryuqqq.archrules.hexagonal.fixture.compliant.layers.adapter.out.CleanAdapter.class,
                com.ryuqqq.archrules.hexagonal.fixture.compliant.layers.domain.CleanLayerDomain.class)
                .hasViolation(), "adapter.out→domain 은 올바른 방향");
    }

    @Test
    void localDateTimeInDomainViolatesFrameworkFree() {
        assertTrue(Runner.check(
                rule,
                com.ryuqqq.archrules.hexagonal.fixture.violation.domain.LocalDateTimeDomain.class)
                .hasViolation());
    }

    @Test
    void instantFieldInDomainPassesFrameworkFree() {
        // Instant는 aggregate의 createdAt/updatedAt 의무 필드 — framework-free를 통과해야 한다.
        assertFalse(Runner.check(rule, InstantDomain.class).hasViolation());
    }
}
