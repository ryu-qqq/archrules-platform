package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

/** Criteria(복합 검색 조건) 작성 컨벤션 — 상대 매처 ..query.criteria.. 로 root 무관. */
final class CriteriaRules {

    private CriteriaRules() {}

    private static final String CRITERIA = "..query.criteria..";

    /**
     * 규칙 1: *Criteria 이름의 클래스는 ..query.criteria.. 패키지에 위치해야 한다.
     */
    static final ArchRule CRITERIA_RESIDES_IN_PACKAGE =
            classes().that()
                    .haveSimpleNameEndingWith("Criteria")
                    .and().resideInAPackage("..domain..")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .should().resideInAPackage(CRITERIA)
                    .as("criteria resides in criteria package")
                    .because("Criteria 클래스는 ..query.criteria.. 패키지에 위치해야 한다")
                    .allowEmptyShould(true);

    /**
     * 규칙 2: criteria 패키지의 구체 클래스는 record여야 한다.
     */
    static final ArchRule CRITERIA_IS_RECORD =
            classes().that()
                    .resideInAPackage(CRITERIA)
                    .and().haveSimpleNameNotContaining("Test")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .should(ArchConditions.beRecords())
                    .as("criteria is record")
                    .because("Criteria는 Java record로 구현한다")
                    .allowEmptyShould(true);

    /**
     * 규칙 3: criteria 패키지의 구체 클래스는 이름이 *Criteria로 끝나야 한다.
     */
    static final ArchRule CRITERIA_NAME_ENDS_WITH_CRITERIA =
            classes().that()
                    .resideInAPackage(CRITERIA)
                    .and().haveSimpleNameNotContaining("Test")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .should().haveSimpleNameEndingWith("Criteria")
                    .as("criteria name ends with Criteria")
                    .because("criteria 패키지의 클래스는 *Criteria 이름 규약을 따른다")
                    .allowEmptyShould(true);

    /**
     * 규칙 4: criteria 패키지의 구체 클래스는 of() 정적 팩토리를 가져야 한다.
     */
    static final ArchRule CRITERIA_HAS_OF =
            classes().that()
                    .resideInAPackage(CRITERIA)
                    .and().haveSimpleNameNotContaining("Test")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .should(ArchConditions.haveStaticMethodWithName("of"))
                    .as("criteria has of")
                    .because("Criteria는 of() 정적 팩토리로 생성한다")
                    .allowEmptyShould(true);

    /**
     * 규칙 8: criteria는 ..application.. / ..adapter.. 에 의존하지 않아야 한다.
     * (원본 화이트리스트 방식을 블랙리스트 상대 매처로 전환 — 도메인 순수성 의도 동일.)
     */
    static final ArchRule CRITERIA_DOES_NOT_DEPEND_ON_OUTER_LAYERS =
            noClasses().that()
                    .resideInAPackage(CRITERIA)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..adapter..")
                    .as("criteria does not depend on outer layers")
                    .because("Criteria는 Domain Layer 내부에만 의존해야 한다 — application·adapter 금지")
                    .allowEmptyShould(true);

    /**
     * 규칙 10: criteria 패키지의 구체 클래스는 public이어야 한다.
     */
    static final ArchRule CRITERIA_IS_PUBLIC =
            classes().that()
                    .resideInAPackage(CRITERIA)
                    .and().haveSimpleNameNotContaining("Test")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .should().bePublic()
                    .as("criteria is public")
                    .because("Criteria는 여러 레이어에서 사용되므로 public이어야 한다")
                    .allowEmptyShould(true);

    static Map<String, ArchRuleSpec> rules() {
        return Map.of(
                "criteria resides in criteria package",
                        new ArchRuleSpec(CRITERIA_RESIDES_IN_PACKAGE, Priority.MEDIUM),
                "criteria is record",
                        new ArchRuleSpec(CRITERIA_IS_RECORD, Priority.HIGH),
                "criteria name ends with Criteria",
                        new ArchRuleSpec(CRITERIA_NAME_ENDS_WITH_CRITERIA, Priority.MEDIUM),
                "criteria has of",
                        new ArchRuleSpec(CRITERIA_HAS_OF, Priority.HIGH),
                "criteria does not depend on outer layers",
                        new ArchRuleSpec(CRITERIA_DOES_NOT_DEPEND_ON_OUTER_LAYERS, Priority.HIGH),
                "criteria is public",
                        new ArchRuleSpec(CRITERIA_IS_PUBLIC, Priority.MEDIUM));
    }
}
