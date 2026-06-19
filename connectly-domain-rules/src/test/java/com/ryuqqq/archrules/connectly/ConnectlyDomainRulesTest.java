package com.ryuqqq.archrules.connectly;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.connectly.fixture.compliant.domain.vo.Color;
import com.ryuqqq.archrules.connectly.fixture.compliant.domain.vo.UserId;
import com.ryuqqq.archrules.connectly.fixture.violation.domain.vo.NoForNewId;
import com.ryuqqq.archrules.connectly.fixture.violation.domain.vo.NoIsNewId;
import com.ryuqqq.archrules.connectly.fixture.violation.domain.vo.PlainStatus;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ConnectlyDomainRulesTest {

    private final ConnectlyDomainRules svc = new ConnectlyDomainRules();
    private ArchRule rule(String key) { return svc.getRules().get(key).rule(); }

    @Test
    void compliantIdAndEnumPass() {
        assertFalse(Runner.check(rule("connectly id VO has forNew"), UserId.class).hasViolation());
        assertFalse(Runner.check(rule("connectly long id VO has isNew"), UserId.class).hasViolation());
        assertFalse(Runner.check(rule("connectly enum VO has displayName"), Color.class).hasViolation());
    }

    @Test
    void missingForNewViolates() {
        assertTrue(Runner.check(rule("connectly id VO has forNew"), NoForNewId.class).hasViolation());
    }

    @Test
    void missingIsNewViolates() {
        assertTrue(Runner.check(rule("connectly long id VO has isNew"), NoIsNewId.class).hasViolation());
    }

    @Test
    void missingDisplayNameViolates() {
        assertTrue(Runner.check(rule("connectly enum VO has displayName"), PlainStatus.class).hasViolation());
    }
}
