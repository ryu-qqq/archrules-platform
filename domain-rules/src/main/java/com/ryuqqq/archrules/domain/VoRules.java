package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

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
                    .should(beRecords())
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
                    .should(haveStaticMethodWithName("of"))
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
                    .should(notHaveMethodsWithNameStartingWith("create"))
                    .as("domain VO has no create method")
                    .because("Value Object는 create*() 대신 of()/forNew()를 쓴다")
                    .allowEmptyShould(true);

    /** Java record 여부(java.lang.Record 상속) 검증. */
    private static ArchCondition<JavaClass> beRecords() {
        return new ArchCondition<JavaClass>("be records") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean isRecord = javaClass.getAllRawSuperclasses().stream()
                        .anyMatch(s -> s.getName().equals("java.lang.Record"));
                if (!isRecord) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            String.format("Class %s is not a record", javaClass.getName())));
                }
            }
        };
    }

    /** public static 메서드 존재 검증. */
    private static ArchCondition<JavaClass> haveStaticMethodWithName(String methodName) {
        return new ArchCondition<JavaClass>("have public static method named " + methodName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean has = javaClass.getAllMethods().stream()
                        .anyMatch(m -> m.getName().equals(methodName)
                                && m.getModifiers().contains(JavaModifier.STATIC)
                                && m.getModifiers().contains(JavaModifier.PUBLIC));
                if (!has) {
                    events.add(SimpleConditionEvent.violated(javaClass, String.format(
                            "Class %s has no public static method '%s'", javaClass.getName(), methodName)));
                }
            }
        };
    }

    /** 특정 접두 메서드 부재 검증. */
    private static ArchCondition<JavaClass> notHaveMethodsWithNameStartingWith(String prefix) {
        return new ArchCondition<JavaClass>("not have methods named starting with " + prefix) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getAllMethods().stream()
                        .filter(m -> m.getName().startsWith(prefix))
                        .forEach(m -> events.add(SimpleConditionEvent.violated(javaClass, String.format(
                                "Class %s has prohibited method '%s' starting with '%s'",
                                javaClass.getName(), m.getName(), prefix))));
            }
        };
    }
}
