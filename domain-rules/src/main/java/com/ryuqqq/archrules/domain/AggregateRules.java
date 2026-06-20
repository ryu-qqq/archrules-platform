package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.time.Instant;
import java.util.Map;

/** Aggregate Root 작성 컨벤션 — 상대 매처 ..aggregate.. 로 root 무관. */
final class AggregateRules {

    private AggregateRules() {}

    private static final String AGG = "..aggregate..";

    // ===== 규칙 4: setter 금지 =====
    static final ArchRule AGG_NO_SETTERS =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().haveSimpleNameNotEndingWith("Id")
                    .and().haveSimpleNameNotEndingWith("Event")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(notHaveSetterMethods())
                    .as("aggregate has no setters")
                    .because("Aggregate는 비즈니스 메서드로 상태 변경 (Setter 금지)")
                    .allowEmptyShould(true);

    // ===== 규칙 5: 생성자 private =====
    static final ArchRule AGG_CONSTRUCTOR_PRIVATE =
            constructors()
                    .that()
                    .areDeclaredInClassesThat().resideInAPackage(AGG)
                    .and().areDeclaredInClassesThat().areNotInterfaces()
                    .and().areDeclaredInClassesThat().areNotEnums()
                    .and().areDeclaredInClassesThat().areNotAnonymousClasses()
                    .and().areDeclaredInClassesThat().areNotMemberClasses()
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Id")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Event")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Exception")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Status")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Fixture")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Mother")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotContaining("Test")
                    .should().bePrivate()
                    .as("aggregate constructor is private")
                    .because("정적 팩토리 메서드(forNew, of, reconstitute)로만 생성해야 합니다")
                    .allowEmptyShould(true);

    // ===== 규칙 6: forNew() =====
    static final ArchRule AGG_HAS_FOR_NEW =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .and().haveSimpleNameNotEndingWith("Id")
                    .and().haveSimpleNameNotEndingWith("Event")
                    .and().haveSimpleNameNotEndingWith("Exception")
                    .and().haveSimpleNameNotEndingWith("Status")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(ArchConditions.haveStaticMethodWithName("forNew"))
                    .as("aggregate has forNew")
                    .because("신규 생성용 팩토리 메서드 forNew() 필수")
                    .allowEmptyShould(true);

    // ===== 규칙 7: of() =====
    static final ArchRule AGG_HAS_OF =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .and().haveSimpleNameNotEndingWith("Id")
                    .and().haveSimpleNameNotEndingWith("Event")
                    .and().haveSimpleNameNotEndingWith("Exception")
                    .and().haveSimpleNameNotEndingWith("Status")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(ArchConditions.haveStaticMethodWithName("of"))
                    .as("aggregate has of")
                    .because("ID 기반 생성용 팩토리 메서드 of() 필수")
                    .allowEmptyShould(true);

    // ===== 규칙 8: reconstitute() =====
    static final ArchRule AGG_HAS_RECONSTITUTE =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .and().haveSimpleNameNotEndingWith("Id")
                    .and().haveSimpleNameNotEndingWith("Event")
                    .and().haveSimpleNameNotEndingWith("Exception")
                    .and().haveSimpleNameNotEndingWith("Status")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(ArchConditions.haveStaticMethodWithName("reconstitute"))
                    .as("aggregate has reconstitute")
                    .because("영속성 복원용 팩토리 메서드 reconstitute() 필수")
                    .allowEmptyShould(true);

    // ===== 규칙 9: id 필드 final =====
    static final ArchRule AGG_ID_FIELD_FINAL =
            fields().that()
                    .areDeclaredInClassesThat().resideInAPackage(AGG)
                    .and().areDeclaredInClassesThat().areNotInterfaces()
                    .and().areDeclaredInClassesThat().areNotEnums()
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Id")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Event")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Exception")
                    .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Status")
                    .and().haveNameMatching("id")
                    .should().beFinal()
                    .as("aggregate id field is final")
                    .because("Aggregate ID는 불변이어야 합니다")
                    .allowEmptyShould(true);

    // ===== 규칙 10: Instant 필드 최소 1개 =====
    static final ArchRule AGG_HAS_INSTANT_FIELD =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .and().haveSimpleNameNotEndingWith("Id")
                    .and().haveSimpleNameNotEndingWith("Event")
                    .and().haveSimpleNameNotEndingWith("Exception")
                    .and().haveSimpleNameNotEndingWith("Status")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(haveFieldOfType(Instant.class))
                    .as("aggregate has instant field")
                    .because("시간 처리를 위해 Instant 필드 필수 (Clock은 파라미터로 주입받아 Instant로 변환)")
                    .allowEmptyShould(true);

    // ===== 규칙 11: 외래키는 VO (원시타입 금지) =====
    static final ArchRule AGG_FOREIGN_KEY_IS_VO =
            noFields()
                    .that()
                    .areDeclaredInClassesThat().resideInAPackage(AGG)
                    .and().areDeclaredInClassesThat().areNotInterfaces()
                    .and().areDeclaredInClassesThat().areNotEnums()
                    .and().haveNameMatching(".*[Ii]d")
                    .and().doNotHaveName("id")
                    .should().haveRawType(Long.class)
                    .orShould().haveRawType(String.class)
                    .orShould().haveRawType(Integer.class)
                    .orShould().haveRawType(long.class)
                    .orShould().haveRawType(int.class)
                    .as("aggregate foreign key is value object")
                    .because("외래키는 VO 사용 (Long paymentId -> PaymentId paymentId)")
                    .allowEmptyShould(true);

    // ===== 규칙 12: ..aggregate.. 위치 =====
    static final ArchRule AGG_IN_AGGREGATE_PACKAGE =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().haveSimpleNameNotEndingWith("Id")
                    .and().haveSimpleNameNotEndingWith("Event")
                    .and().haveSimpleNameNotEndingWith("Exception")
                    .and().haveSimpleNameNotEndingWith("Status")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should().resideInAPackage("..aggregate..")
                    .as("aggregate resides in aggregate package")
                    .because("Aggregate는 aggregate 패키지에 위치해야 합니다")
                    .allowEmptyShould(true);

    // ===== 규칙 13: public 클래스 =====
    static final ArchRule AGG_IS_PUBLIC =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should().bePublic()
                    .as("aggregate is public")
                    .because("다른 레이어에서 사용하기 위해 public 필수")
                    .allowEmptyShould(true);

    // ===== 규칙 14: final 클래스 금지 =====
    static final ArchRule AGG_NOT_FINAL =
            classes().that().resideInAPackage(AGG)
                    .and().areNotInterfaces()
                    .and().areNotEnums()
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .and().haveSimpleNameNotEndingWith("Id")
                    .and().haveSimpleNameNotEndingWith("Event")
                    .and().haveSimpleNameNotEndingWith("Exception")
                    .and().haveSimpleNameNotEndingWith("Status")
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(notBeFinal())
                    .as("aggregate is not final")
                    .because("확장 가능성을 위해 final 금지")
                    .allowEmptyShould(true);

    // ===== 규칙 16: 외부 레이어 의존 금지 =====
    static final ArchRule AGG_NO_OUTER_LAYERS =
            noClasses().that().resideInAPackage(AGG)
                    .should().dependOnClassesThat().resideInAnyPackage("..application..", "..adapter..")
                    .as("aggregate does not depend on outer layers")
                    .because("헥사고날 아키텍처: Domain은 외부 레이어에 의존 금지")
                    .allowEmptyShould(true);

    // ===== 규칙 17: createdAt — Instant·final =====
    static final ArchRule AGG_CREATED_AT_INSTANT_AND_FINAL =
            CompositeArchRule.of(
                    fields().that()
                            .areDeclaredInClassesThat().resideInAPackage(AGG)
                            .and().areDeclaredInClassesThat().areNotInterfaces()
                            .and().areDeclaredInClassesThat().areNotEnums()
                            .and().haveNameMatching("createdAt")
                            .should().haveRawType(Instant.class)
                            .allowEmptyShould(true)
                            .because("시간 필드는 Instant 사용 (LocalDateTime 금지)")
            ).and(
                    fields().that()
                            .areDeclaredInClassesThat().resideInAPackage(AGG)
                            .and().areDeclaredInClassesThat().areNotInterfaces()
                            .and().areDeclaredInClassesThat().areNotEnums()
                            .and().haveNameMatching("createdAt")
                            .should().beFinal()
                            .allowEmptyShould(true)
                            .because("createdAt은 불변이어야 합니다")
            ).as("aggregate createdAt is instant and final")
            .because("createdAt은 Instant 타입이고 final이어야 합니다")
            .allowEmptyShould(true);

    // ===== 규칙 18: updatedAt — Instant·not final =====
    static final ArchRule AGG_UPDATED_AT_INSTANT_NOT_FINAL =
            CompositeArchRule.of(
                    fields().that()
                            .areDeclaredInClassesThat().resideInAPackage(AGG)
                            .and().areDeclaredInClassesThat().areNotInterfaces()
                            .and().areDeclaredInClassesThat().areNotEnums()
                            .and().haveNameMatching("updatedAt")
                            .should().haveRawType(Instant.class)
                            .allowEmptyShould(true)
                            .because("시간 필드는 Instant 사용 (LocalDateTime 금지)")
            ).and(
                    fields().that()
                            .areDeclaredInClassesThat().resideInAPackage(AGG)
                            .and().areDeclaredInClassesThat().areNotInterfaces()
                            .and().areDeclaredInClassesThat().areNotEnums()
                            .and().haveNameMatching("updatedAt")
                            .should().notBeFinal()
                            .allowEmptyShould(true)
                            .because("updatedAt은 상태 변경 시 갱신되어야 합니다")
            ).as("aggregate updatedAt is instant and not final")
            .because("updatedAt은 Instant 타입이고 non-final이어야 합니다")
            .allowEmptyShould(true);

    // ===== rules() 맵 (14개 규칙, Map.ofEntries 사용) =====

    static Map<String, ArchRuleSpec> rules() {
        return Map.ofEntries(
                Map.entry("aggregate has no setters",
                        new ArchRuleSpec(AGG_NO_SETTERS, Priority.HIGH)),
                Map.entry("aggregate constructor is private",
                        new ArchRuleSpec(AGG_CONSTRUCTOR_PRIVATE, Priority.HIGH)),
                Map.entry("aggregate has forNew",
                        new ArchRuleSpec(AGG_HAS_FOR_NEW, Priority.HIGH)),
                Map.entry("aggregate has of",
                        new ArchRuleSpec(AGG_HAS_OF, Priority.HIGH)),
                Map.entry("aggregate has reconstitute",
                        new ArchRuleSpec(AGG_HAS_RECONSTITUTE, Priority.HIGH)),
                Map.entry("aggregate id field is final",
                        new ArchRuleSpec(AGG_ID_FIELD_FINAL, Priority.MEDIUM)),
                Map.entry("aggregate has instant field",
                        new ArchRuleSpec(AGG_HAS_INSTANT_FIELD, Priority.MEDIUM)),
                Map.entry("aggregate foreign key is value object",
                        new ArchRuleSpec(AGG_FOREIGN_KEY_IS_VO, Priority.HIGH)),
                Map.entry("aggregate resides in aggregate package",
                        new ArchRuleSpec(AGG_IN_AGGREGATE_PACKAGE, Priority.MEDIUM)),
                Map.entry("aggregate is public",
                        new ArchRuleSpec(AGG_IS_PUBLIC, Priority.MEDIUM)),
                Map.entry("aggregate is not final",
                        new ArchRuleSpec(AGG_NOT_FINAL, Priority.MEDIUM)),
                Map.entry("aggregate does not depend on outer layers",
                        new ArchRuleSpec(AGG_NO_OUTER_LAYERS, Priority.HIGH)),
                Map.entry("aggregate createdAt is instant and final",
                        new ArchRuleSpec(AGG_CREATED_AT_INSTANT_AND_FINAL, Priority.MEDIUM)),
                Map.entry("aggregate updatedAt is instant and not final",
                        new ArchRuleSpec(AGG_UPDATED_AT_INSTANT_NOT_FINAL, Priority.MEDIUM))
        );
    }

    // ===== 카테고리 전용 private static ArchCondition =====

    /** setter(set[A-Z].* public) 메서드 부재 검증. */
    private static ArchCondition<JavaClass> notHaveSetterMethods() {
        return new ArchCondition<JavaClass>("not have setter methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().stream()
                        .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                        .filter(method -> method.getName().matches("set[A-Z].*"))
                        .forEach(method -> {
                            String message = String.format(
                                    "클래스 %s가 setter 메서드 %s()를 가지고 있습니다 (Setter 금지)",
                                    javaClass.getSimpleName(), method.getName());
                            events.add(SimpleConditionEvent.violated(javaClass, message));
                        });
            }
        };
    }

    /** 특정 타입의 필드 존재 검증. */
    private static ArchCondition<JavaClass> haveFieldOfType(Class<?> fieldType) {
        return new ArchCondition<JavaClass>("have field of type: " + fieldType.getSimpleName()) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasField = javaClass.getAllFields().stream()
                        .anyMatch(field -> field.getRawType().isEquivalentTo(fieldType));
                if (!hasField) {
                    String message = String.format(
                            "클래스 %s가 %s 타입 필드를 가지고 있지 않습니다",
                            javaClass.getSimpleName(), fieldType.getSimpleName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /** final 클래스 금지 검증. */
    private static ArchCondition<JavaClass> notBeFinal() {
        return new ArchCondition<JavaClass>("not be final") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                if (javaClass.getModifiers().contains(JavaModifier.FINAL)) {
                    String message = String.format(
                            "클래스 %s가 final로 선언되어 있습니다 (확장 가능성을 위해 final 금지)",
                            javaClass.getSimpleName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
