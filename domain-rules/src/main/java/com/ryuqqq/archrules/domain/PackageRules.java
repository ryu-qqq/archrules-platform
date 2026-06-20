package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import java.util.Map;

/** BC 패키지 구조 규칙 — 상대 매처로 root 무관. */
final class PackageRules {

    private PackageRules() {}

    private static final String COMMON_UTIL = "..common.util..";
    private static final String EVENT       = "..event..";
    private static final String EXCEPTION   = "..exception..";

    /**
     * 규칙 2: ..common.util.. 패키지는 인터페이스만 허용.
     * DIP — 구현체는 Application Layer에 위치해야 한다.
     */
    static final ArchRule UTIL_IS_INTERFACE =
            classes().that().resideInAPackage(COMMON_UTIL)
                    .should().beInterfaces()
                    .as("domain common util is interface only")
                    .because("domain.common.util 패키지는 DIP를 위해 인터페이스만 포함해야 한다(구현은 Application Layer)")
                    .allowEmptyShould(true);

    /**
     * 규칙 3: DomainEvent 구현체는 ..event.. 패키지에 위치.
     * 마커 식별: 이름이 DomainEvent 인 인터페이스를 implement 한 클래스.
     */
    static final ArchRule EVENT_IN_EVENT_PACKAGE =
            classes().that()
                    .implement(domainEventMarker())
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().doNotHaveSimpleName("DomainEvent")
                    .should().resideInAPackage(EVENT)
                    .as("domain event resides in event package")
                    .because("DomainEvent 구현체는 ..event.. 패키지에 위치해야 한다")
                    .allowEmptyShould(true);

    /**
     * 규칙 4: DomainException 서브타입은 ..exception.. 패키지에 위치.
     * 마커 식별: 이름이 DomainException 인 클래스의 서브타입.
     */
    static final ArchRule EXCEPTION_IN_EXCEPTION_PACKAGE =
            classes().that()
                    .areAssignableTo(domainExceptionMarker())
                    .and().haveSimpleNameNotContaining("Test")
                    .and().doNotHaveSimpleName("DomainException")
                    .should().resideInAPackage(EXCEPTION)
                    .as("domain exception resides in exception package")
                    .because("DomainException 서브타입은 ..exception.. 패키지에 위치해야 한다")
                    .allowEmptyShould(true);

    /**
     * 규칙 5: Bounded Context 간 순환 의존 금지.
     * ..domain.(*).." 패턴으로 BC 슬라이스를 캡처해 순환 검사.
     */
    static final ArchRule BC_CYCLE_FREE =
            SlicesRuleDefinition.slices()
                    .matching("..domain.(*)..")
                    .should().beFreeOfCycles()
                    .as("bounded contexts are cycle-free")
                    .because("Bounded Context 간 순환 의존성은 설계 오염을 유발한다(Long FK 전략 사용)");

    static Map<String, ArchRuleSpec> rules() {
        return Map.of(
                "domain common util is interface only",
                        new ArchRuleSpec(UTIL_IS_INTERFACE, Priority.MEDIUM),
                "domain event resides in event package",
                        new ArchRuleSpec(EVENT_IN_EVENT_PACKAGE, Priority.MEDIUM),
                "domain exception resides in exception package",
                        new ArchRuleSpec(EXCEPTION_IN_EXCEPTION_PACKAGE, Priority.MEDIUM),
                "bounded contexts are cycle-free",
                        new ArchRuleSpec(BC_CYCLE_FREE, Priority.HIGH));
    }

    /** 이름이 DomainEvent 인 인터페이스를 식별하는 predicate (이름 기반, 이식성). */
    private static DescribedPredicate<JavaClass> domainEventMarker() {
        return new DescribedPredicate<JavaClass>("named DomainEvent and is interface") {
            @Override
            public boolean test(JavaClass javaClass) {
                return "DomainEvent".equals(javaClass.getSimpleName()) && javaClass.isInterface();
            }
        };
    }

    /** 이름이 DomainException 인 클래스를 식별하는 predicate (이름 기반, 이식성). */
    private static DescribedPredicate<JavaClass> domainExceptionMarker() {
        return new DescribedPredicate<JavaClass>("named DomainException") {
            @Override
            public boolean test(JavaClass javaClass) {
                return "DomainException".equals(javaClass.getSimpleName());
            }
        };
    }
}
