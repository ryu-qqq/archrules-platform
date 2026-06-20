package com.ryuqqq.archrules.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.aggregate.PureAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.FinalAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.FinalUpdatedAtAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.NoForNewAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.NoInstantAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.NoOfAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.NoReconstituteAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.NonFinalCreatedAtAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.NonFinalIdAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.OuterLayerDependentAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.PackagePrivateAggregateAccess;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.PrimitiveForeignKeyAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.PublicConstructorAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.WithSetterAggregate;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.application.ApplicationService;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class AggregateRulesTest {

    private ArchRule rule(String key) {
        return AggregateRules.rules().get(key).rule();
    }

    // ===== compliant 전체 통과 =====

    @Test
    void compliantAggregatePassesAllRules() {
        assertFalse(Runner.check(rule("aggregate has no setters"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate constructor is private"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate has forNew"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate has of"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate has reconstitute"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate id field is final"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate has instant field"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate resides in aggregate package"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate is public"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate is not final"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate does not depend on outer layers"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate createdAt is instant and final"), PureAggregate.class).hasViolation());
        assertFalse(Runner.check(rule("aggregate updatedAt is instant and not final"), PureAggregate.class).hasViolation());
    }

    // ===== 규칙 4: setter 금지 =====

    @Test
    void setterViolatesNoSettersRule() {
        assertTrue(Runner.check(rule("aggregate has no setters"), WithSetterAggregate.class).hasViolation());
    }

    // ===== 규칙 5: 생성자 private =====

    @Test
    void publicConstructorViolatesPrivateConstructorRule() {
        assertTrue(Runner.check(rule("aggregate constructor is private"), PublicConstructorAggregate.class).hasViolation());
    }

    // ===== 규칙 6: forNew() =====

    @Test
    void missingForNewViolatesForNewRule() {
        assertTrue(Runner.check(rule("aggregate has forNew"), NoForNewAggregate.class).hasViolation());
    }

    // ===== 규칙 7: of() =====

    @Test
    void missingOfViolatesOfRule() {
        assertTrue(Runner.check(rule("aggregate has of"), NoOfAggregate.class).hasViolation());
    }

    // ===== 규칙 8: reconstitute() =====

    @Test
    void missingReconstituteViolatesReconstituteRule() {
        assertTrue(Runner.check(rule("aggregate has reconstitute"), NoReconstituteAggregate.class).hasViolation());
    }

    // ===== 규칙 9: id 필드 final =====

    @Test
    void nonFinalIdViolatesIdFinalRule() {
        assertTrue(Runner.check(rule("aggregate id field is final"), NonFinalIdAggregate.class).hasViolation());
    }

    // ===== 규칙 10: Instant 필드 =====

    @Test
    void noInstantFieldViolatesInstantRule() {
        assertTrue(Runner.check(rule("aggregate has instant field"), NoInstantAggregate.class).hasViolation());
    }

    // ===== 규칙 11: 외래키 VO =====

    @Test
    void primitiveForeignKeyViolatesForeignKeyRule() {
        assertTrue(Runner.check(rule("aggregate foreign key is value object"), PrimitiveForeignKeyAggregate.class).hasViolation());
    }

    // ===== 규칙 13: public 클래스 =====

    @Test
    void packagePrivateViolatesPublicRule() {
        assertTrue(Runner.check(rule("aggregate is public"), PackagePrivateAggregateAccess.target()).hasViolation());
    }

    // ===== 규칙 14: final 금지 =====

    @Test
    void finalClassViolatesNotFinalRule() {
        assertTrue(Runner.check(rule("aggregate is not final"), FinalAggregate.class).hasViolation());
    }

    // ===== 규칙 16: 외부 레이어 의존 금지 =====

    @Test
    void outerLayerDependencyViolatesOuterLayerRule() {
        assertTrue(Runner.check(
                rule("aggregate does not depend on outer layers"),
                OuterLayerDependentAggregate.class,
                ApplicationService.class).hasViolation());
    }

    // ===== 규칙 17: createdAt Instant·final =====

    @Test
    void nonFinalCreatedAtViolatesCreatedAtRule() {
        assertTrue(Runner.check(rule("aggregate createdAt is instant and final"), NonFinalCreatedAtAggregate.class).hasViolation());
    }

    // ===== 규칙 18: updatedAt Instant·not final =====

    @Test
    void finalUpdatedAtViolatesUpdatedAtRule() {
        assertTrue(Runner.check(rule("aggregate updatedAt is instant and not final"), FinalUpdatedAtAggregate.class).hasViolation());
    }
}
