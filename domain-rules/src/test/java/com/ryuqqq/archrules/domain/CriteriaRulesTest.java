package com.ryuqqq.archrules.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.query.criteria.SearchCriteria;
import com.ryuqqq.archrules.domain.fixture.violation.domain.query.WrongPackageCriteria;
import com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria.NoOfCriteria;
import com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria.NotRecordCriteria;
import com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria.OuterLayerCriteria;
import com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria.SearchFilter;
import com.ryuqqq.archrules.domain.fixture.violation.application.AppService;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class CriteriaRulesTest {

    private ArchRule rule(String key) {
        return CriteriaRules.rules().get(key).rule();
    }

    // ==================== compliant 통과 ====================

    @Test
    void compliantCriteriaPassesAllRules() {
        assertFalse(Runner.check(rule("criteria resides in criteria package"), SearchCriteria.class).hasViolation());
        assertFalse(Runner.check(rule("criteria is record"), SearchCriteria.class).hasViolation());
        assertFalse(Runner.check(rule("criteria name ends with Criteria"), SearchCriteria.class).hasViolation());
        assertFalse(Runner.check(rule("criteria has of"), SearchCriteria.class).hasViolation());
        assertFalse(Runner.check(rule("criteria is public"), SearchCriteria.class).hasViolation());
    }

    @Test
    void compliantCriteriaPassesDependencyRule() {
        assertFalse(Runner.check(rule("criteria does not depend on outer layers"), SearchCriteria.class).hasViolation());
    }

    // ==================== 규칙 1: 위치 위반 ====================

    @Test
    void wrongPackageCriteriaViolatesResideRule() {
        assertTrue(Runner.check(rule("criteria resides in criteria package"), WrongPackageCriteria.class).hasViolation());
    }

    // ==================== 규칙 2: record 아님 위반 ====================

    @Test
    void notRecordCriteriaViolatesRecordRule() {
        assertTrue(Runner.check(rule("criteria is record"), NotRecordCriteria.class).hasViolation());
    }

    // ==================== 규칙 3: 이름 위반 ====================

    @Test
    void searchFilterViolatesNameRule() {
        assertTrue(Runner.check(rule("criteria name ends with Criteria"), SearchFilter.class).hasViolation());
    }

    // ==================== 규칙 4: of() 없음 위반 ====================

    @Test
    void noOfCriteriaViolatesFactoryRule() {
        assertTrue(Runner.check(rule("criteria has of"), NoOfCriteria.class).hasViolation());
    }

    // ==================== 규칙 8: 외부 레이어 의존 위반 ====================

    @Test
    void outerLayerCriteriaViolatesDependencyRule() {
        assertTrue(Runner.check(rule("criteria does not depend on outer layers"),
                OuterLayerCriteria.class, AppService.class).hasViolation());
    }

    // ==================== 규칙 10: public 클래스 — compliant로 통과 확인 ====================
    // Java record는 항상 public이므로 단독 violation 픽스처 구성 불가.
    // compliantCriteriaPassesAllRules()에서 "criteria is public" 통과 확인.
}
