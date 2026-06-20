package com.ryuqqq.archrules.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.context.fixture.compliant.alpha.adapter.AlphaToBetaBridge;
import com.ryuqqq.archrules.context.fixture.compliant.alpha.application.AlphaService;
import com.ryuqqq.archrules.context.fixture.compliant.alpha.domain.AlphaAggregate;
import com.ryuqqq.archrules.context.fixture.compliant.beta.api.BetaUseCase;
import com.ryuqqq.archrules.context.fixture.violation.alpha.application.LeakyAlphaService;
import com.ryuqqq.archrules.context.fixture.violation.beta.domain.BetaAggregate;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ContextIsolationRulesTest {

    private final ArchRule noCrossInternals =
            new ContextIsolationRules().getRules().get("no cross-context internals").rule();

    @Test
    void ruleIsExposed() {
        assertNotNull(noCrossInternals);
    }

    @Test
    void sameContextApplicationToDomainPasses() {
        assertFalse(Runner.check(noCrossInternals, AlphaService.class, AlphaAggregate.class)
                .hasViolation(), "같은 컨텍스트 application→domain 은 허용");
    }

    @Test
    void bridgeAdapterToForeignApiPasses() {
        assertFalse(Runner.check(noCrossInternals, AlphaToBetaBridge.class, BetaUseCase.class)
                .hasViolation(), "어댑터→다른 컨텍스트 .api 는 허용(교차 seam)");
    }

    @Test
    void applicationToForeignDomainViolates() {
        assertTrue(Runner.check(noCrossInternals, LeakyAlphaService.class, BetaAggregate.class)
                .hasViolation(), "application→다른 컨텍스트 domain 은 위반");
    }
}
