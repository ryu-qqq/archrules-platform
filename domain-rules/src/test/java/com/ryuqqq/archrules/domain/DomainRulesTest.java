package com.ryuqqq.archrules.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.PureOrder;
import com.ryuqqq.archrules.domain.fixture.violation.domain.MutableDomain;
import com.ryuqqq.archrules.domain.fixture.violation.domain.TimeReadingDomain;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class DomainRulesTest {

    private final DomainRules svc = new DomainRules();
    private ArchRule rule(String key) { return svc.getRules().get(key).rule(); }

    @Test
    void compliantDomainPassesBoth() {
        assertFalse(Runner.check(rule("domain reads no clock"), PureOrder.class).hasViolation());
        assertFalse(Runner.check(rule("domain has no setters"), PureOrder.class).hasViolation());
    }

    @Test
    void clockReadingDomainViolates() {
        assertTrue(Runner.check(rule("domain reads no clock"), TimeReadingDomain.class).hasViolation());
    }

    @Test
    void setterDomainViolates() {
        assertTrue(Runner.check(rule("domain has no setters"), MutableDomain.class).hasViolation());
    }
}
