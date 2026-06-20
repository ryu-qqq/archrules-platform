package com.ryuqqq.archrules.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.exception.CleanErrorCode;
import com.ryuqqq.archrules.domain.fixture.compliant.domain.exception.CleanException;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.AppDependentException;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.CheckedException;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.NoGetCodeErrorCode;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.NoGetHttpStatusErrorCode;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.NoGetMessageErrorCode;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.NoInterfaceErrorCode;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.NoDomainParentException;
import com.ryuqqq.archrules.domain.fixture.violation.domain.exception.ViolationFixtures;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ExceptionRulesTest {

    private ArchRule rule(String key) {
        return ExceptionRules.rules().get(key).rule();
    }

    // ==================== compliant 통과 ====================

    @Test
    void compliantErrorCodePassesAll() {
        assertFalse(Runner.check(rule("errorcode implements ErrorCode interface"), CleanErrorCode.class).hasViolation());
        assertFalse(Runner.check(rule("errorcode resides in exception package"), CleanErrorCode.class).hasViolation());
        assertFalse(Runner.check(rule("errorcode is public"), CleanErrorCode.class).hasViolation());
        assertFalse(Runner.check(rule("errorcode has getCode"), CleanErrorCode.class).hasViolation());
        assertFalse(Runner.check(rule("errorcode has getHttpStatus"), CleanErrorCode.class).hasViolation());
        assertFalse(Runner.check(rule("errorcode has getMessage"), CleanErrorCode.class).hasViolation());
        assertFalse(Runner.check(rule("errorcode getHttpStatus returns non-spring"), CleanErrorCode.class).hasViolation());
    }

    @Test
    void compliantExceptionPassesAll() {
        assertFalse(Runner.check(rule("exception extends DomainException"),
                CleanException.class,
                com.ryuqqq.archrules.domain.fixture.compliant.domain.exception.DomainException.class).hasViolation());
        assertFalse(Runner.check(rule("exception resides in exception package"), CleanException.class).hasViolation());
        assertFalse(Runner.check(rule("exception is public"), CleanException.class).hasViolation());
        assertFalse(Runner.check(rule("exception extends RuntimeException"), CleanException.class).hasViolation());
        assertFalse(Runner.check(rule("exception does not depend on outer layers"), CleanException.class).hasViolation());
    }

    // ==================== 규칙 #1 위반 ====================

    @Test
    void noInterfaceErrorCodeViolatesImplements() {
        assertTrue(Runner.check(rule("errorcode implements ErrorCode interface"), NoInterfaceErrorCode.class).hasViolation());
    }

    // ==================== 규칙 #4 위반 ====================

    @Test
    void packagePrivateErrorCodeViolatesPublic() {
        Class<?> clazz = ViolationFixtures.packagePrivateErrorCodeClass();
        assertTrue(Runner.check(rule("errorcode is public"), clazz).hasViolation());
    }

    // ==================== 규칙 #5 위반 ====================

    @Test
    void noGetCodeViolatesGetCode() {
        assertTrue(Runner.check(rule("errorcode has getCode"), NoGetCodeErrorCode.class).hasViolation());
    }

    // ==================== 규칙 #6 위반 ====================

    @Test
    void noGetHttpStatusViolatesGetHttpStatus() {
        assertTrue(Runner.check(rule("errorcode has getHttpStatus"), NoGetHttpStatusErrorCode.class).hasViolation());
    }

    // ==================== 규칙 #7 위반 ====================

    @Test
    void noGetMessageViolatesGetMessage() {
        assertTrue(Runner.check(rule("errorcode has getMessage"), NoGetMessageErrorCode.class).hasViolation());
    }

    // ==================== 규칙 #9 위반 ====================

    @Test
    void plainRuntimeExceptionViolatesExtendsDomainException() {
        assertTrue(Runner.check(rule("exception extends DomainException"), NoDomainParentException.class).hasViolation());
    }

    // ==================== 규칙 #14 위반 ====================

    @Test
    void packagePrivateExceptionViolatesPublic() {
        Class<?> clazz = ViolationFixtures.packagePrivateExceptionClass();
        assertTrue(Runner.check(rule("exception is public"), clazz).hasViolation());
    }

    // ==================== 규칙 #15 위반 ====================

    @Test
    void checkedExceptionViolatesRuntimeException() {
        assertTrue(Runner.check(rule("exception extends RuntimeException"), CheckedException.class).hasViolation());
    }

    // ==================== 규칙 #18 위반 ====================

    @Test
    void appDependentExceptionViolatesOuterLayerRule() {
        assertTrue(Runner.check(rule("exception does not depend on outer layers"),
                AppDependentException.class,
                com.ryuqqq.archrules.domain.fixture.violation.application.AppService.class).hasViolation());
    }
}
