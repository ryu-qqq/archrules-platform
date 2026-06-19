package com.ryuqqq.archrules.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.vo.CompliantMoney;
import com.ryuqqq.archrules.domain.fixture.violation.domain.vo.CreateMethodVo;
import com.ryuqqq.archrules.domain.fixture.violation.domain.vo.NoFactoryVo;
import com.ryuqqq.archrules.domain.fixture.violation.domain.vo.NotARecordVo;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class VoRulesTest {

    private final DomainRules svc = new DomainRules();
    private ArchRule rule(String key) { return svc.getRules().get(key).rule(); }

    @Test
    void compliantVoPassesAllThree() {
        assertFalse(Runner.check(rule("domain VO is record"), CompliantMoney.class).hasViolation());
        assertFalse(Runner.check(rule("domain VO has static factory of"), CompliantMoney.class).hasViolation());
        assertFalse(Runner.check(rule("domain VO has no create method"), CompliantMoney.class).hasViolation());
    }

    @Test
    void nonRecordViolatesRecordRule() {
        assertTrue(Runner.check(rule("domain VO is record"), NotARecordVo.class).hasViolation());
    }

    @Test
    void missingOfViolatesFactoryRule() {
        assertTrue(Runner.check(rule("domain VO has static factory of"), NoFactoryVo.class).hasViolation());
    }

    @Test
    void createMethodViolatesNoCreateRule() {
        assertTrue(Runner.check(rule("domain VO has no create method"), CreateMethodVo.class).hasViolation());
    }
}
