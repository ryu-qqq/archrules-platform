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
import com.ryuqqq.archrules.context.fixture.violation.delta.api.DeltaUseCase;
import com.ryuqqq.archrules.context.fixture.violation.epsilon.internal.EpsilonInternal;
import com.ryuqqq.archrules.context.fixture.violation.gamma.application.GammaUsesForeignApi;
import com.ryuqqq.archrules.context.fixture.violation.zeta.application.ZetaUsesForeignInternal;
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

    @Test
    void applicationToForeignInternalViolates() {
        assertTrue(Runner.check(noCrossInternals, ZetaUsesForeignInternal.class, EpsilonInternal.class)
                .hasViolation(), "application→다른 컨텍스트 internal 은 위반");
    }

    @Test
    void coreToForeignApiViolates() {
        ArchRule rule = new ContextIsolationRules().getRules().get("core blind to foreign api").rule();
        assertTrue(Runner.check(rule, GammaUsesForeignApi.class, DeltaUseCase.class)
                .hasViolation(), "application→다른 컨텍스트 .api 는 위반(코어는 자기 포트만)");
    }

    @Test
    void bridgeAdapterToForeignApiPassesCoreRule() {
        ArchRule rule = new ContextIsolationRules().getRules().get("core blind to foreign api").rule();
        assertFalse(Runner.check(rule, AlphaToBetaBridge.class, BetaUseCase.class)
                .hasViolation(), "어댑터는 다른 컨텍스트 .api 의존 허용");
    }
}
