package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

/** VO 작성 컨벤션(보편) — 상대 매처 ..vo.. 로 root 무관. */
final class VoRules {

    private VoRules() {}

    private static final String VO = "..vo..";

    static final ArchRule VO_IS_RECORD =
            classes().that().resideInAPackage(VO)
                    .and().areNotEnums()
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(ArchConditions.beRecords())
                    .as("domain VO is record")
                    .because("Value Object는 Java record로 구현한다(Enum/Interface/Abstract 제외)")
                    .allowEmptyShould(true);

    static final ArchRule VO_HAS_OF =
            classes().that().resideInAPackage(VO)
                    .and().areNotEnums()
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(ArchConditions.haveStaticMethodWithName("of"))
                    .as("domain VO has static factory of")
                    .because("Value Object는 of() 정적 팩토리로 생성한다")
                    .allowEmptyShould(true);

    static final ArchRule VO_NO_CREATE =
            classes().that().resideInAPackage(VO)
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(ArchConditions.notHaveMethodsWithNameStartingWith("create"))
                    .as("domain VO has no create method")
                    .because("Value Object는 create*() 대신 of()/forNew()를 쓴다")
                    .allowEmptyShould(true);

    static Map<String, ArchRuleSpec> rules() {
        return Map.of(
                "domain VO is record",
                        new ArchRuleSpec(VO_IS_RECORD, Priority.HIGH),
                "domain VO has static factory of",
                        new ArchRuleSpec(VO_HAS_OF, Priority.HIGH),
                "domain VO has no create method",
                        new ArchRuleSpec(VO_NO_CREATE, Priority.MEDIUM));
    }
}
