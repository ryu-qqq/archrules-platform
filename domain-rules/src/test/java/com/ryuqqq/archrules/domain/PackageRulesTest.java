package com.ryuqqq.archrules.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.domain.bar.BarService;
import com.ryuqqq.archrules.domain.cbar.CyclicBarService;
import com.ryuqqq.archrules.domain.cfoo.CyclicFooService;
import com.ryuqqq.archrules.domain.fixture.compliant.domain.common.util.ValidUtil;
import com.ryuqqq.archrules.domain.fixture.compliant.domain.event.DomainEvent;
import com.ryuqqq.archrules.domain.fixture.compliant.domain.event.OrderShipped;
import com.ryuqqq.archrules.domain.fixture.compliant.domain.pkg.exception.OrderNotFoundException;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate2.MisplacedEvent;
import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate2.WrongPlacedException;
import com.ryuqqq.archrules.domain.fixture.violation.domain.common.util.ConcreteUtil;
import com.ryuqqq.archrules.domain.foo.FooService;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class PackageRulesTest {

    private ArchRule rule(String key) {
        return PackageRules.rules().get(key).rule();
    }

    // ── 규칙 2: ..common.util.. 은 인터페이스만 ──────────────────────────

    @Test
    void interfaceInUtilPackageIsCompliant() {
        assertFalse(
                Runner.check(rule("domain common util is interface only"), ValidUtil.class)
                        .hasViolation());
    }

    @Test
    void concreteClassInUtilPackageViolates() {
        assertTrue(
                Runner.check(rule("domain common util is interface only"), ConcreteUtil.class)
                        .hasViolation());
    }

    // ── 규칙 3: DomainEvent 구현체는 ..event.. 위치 ──────────────────────

    @Test
    void domainEventInEventPackageIsCompliant() {
        assertFalse(
                Runner.check(
                                rule("domain event resides in event package"),
                                OrderShipped.class,
                                DomainEvent.class)
                        .hasViolation());
    }

    @Test
    void domainEventOutsideEventPackageViolates() {
        assertTrue(
                Runner.check(
                                rule("domain event resides in event package"),
                                MisplacedEvent.class,
                                DomainEvent.class)
                        .hasViolation());
    }

    // ── 규칙 4: DomainException 서브타입은 ..exception.. 위치 ────────────

    @Test
    void domainExceptionInExceptionPackageIsCompliant() {
        assertFalse(
                Runner.check(
                                rule("domain exception resides in exception package"),
                                OrderNotFoundException.class)
                        .hasViolation());
    }

    @Test
    void domainExceptionOutsideExceptionPackageViolates() {
        assertTrue(
                Runner.check(
                                rule("domain exception resides in exception package"),
                                WrongPlacedException.class,
                                com.ryuqqq.archrules.domain.fixture.compliant.domain.pkg.exception.DomainException.class)
                        .hasViolation());
    }

    // ── 규칙 5: BC 간 순환 의존 금지 ─────────────────────────────────────

    @Test
    void independentBoundedContextsAreCompliant() {
        assertFalse(
                Runner.check(
                                rule("bounded contexts are cycle-free"),
                                FooService.class,
                                BarService.class)
                        .hasViolation());
    }

    @Test
    void cyclicBoundedContextsViolate() {
        assertTrue(
                Runner.check(
                                rule("bounded contexts are cycle-free"),
                                CyclicFooService.class,
                                CyclicBarService.class)
                        .hasViolation());
    }
}
