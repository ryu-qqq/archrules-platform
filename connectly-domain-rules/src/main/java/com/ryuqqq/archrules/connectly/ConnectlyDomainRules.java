package com.ryuqqq.archrules.connectly;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Map;

/** connectly platform-common-domain VO 컨벤션(특화) — 상대 매처 ..vo.. 로 root 무관. */
public final class ConnectlyDomainRules implements ArchRulesService {

    private static final String VO = "..vo..";

    static final ArchRule ID_HAS_FORNEW =
            classes().that().resideInAPackage(VO)
                    .and().haveSimpleNameEndingWith("Id")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(haveStaticMethodWithName("forNew"))
                    .as("connectly id VO has forNew")
                    .because("ID VO는 forNew() 팩토리를 가진다(Long=null, UUID=UUIDv7)")
                    .allowEmptyShould(true);

    static final ArchRule LONG_ID_HAS_ISNEW =
            classes().that().resideInAPackage(VO)
                    .and().haveSimpleNameEndingWith("Id")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(haveLongFieldAndIsNewMethod())
                    .as("connectly long id VO has isNew")
                    .because("Long ID VO는 isNew()로 신규 여부를 판별한다(UUID 면제)")
                    .allowEmptyShould(true);

    static final ArchRule ENUM_HAS_DISPLAYNAME =
            classes().that().resideInAPackage(VO)
                    .and().areEnums()
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(haveMethodWithName("displayName"))
                    .as("connectly enum VO has displayName")
                    .because("Enum VO는 displayName()으로 표시명을 제공한다")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "connectly id VO has forNew", new ArchRuleSpec(ID_HAS_FORNEW, Priority.HIGH),
                "connectly long id VO has isNew", new ArchRuleSpec(LONG_ID_HAS_ISNEW, Priority.MEDIUM),
                "connectly enum VO has displayName", new ArchRuleSpec(ENUM_HAS_DISPLAYNAME, Priority.MEDIUM));
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

    /** 임의 이름 메서드 존재 검증(static 무관). */
    private static ArchCondition<JavaClass> haveMethodWithName(String methodName) {
        return new ArchCondition<JavaClass>("have method named " + methodName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean has = javaClass.getAllMethods().stream()
                        .anyMatch(m -> m.getName().equals(methodName));
                if (!has) {
                    events.add(SimpleConditionEvent.violated(javaClass, String.format(
                            "Class %s has no method '%s'", javaClass.getName(), methodName)));
                }
            }
        };
    }

    /** Long 필드 보유 ID VO는 isNew() 필수. String(UUID) 전용이면 면제. */
    private static ArchCondition<JavaClass> haveLongFieldAndIsNewMethod() {
        return new ArchCondition<JavaClass>("have Long field and isNew() method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasLongField = javaClass.getAllFields().stream()
                        .anyMatch(f -> f.getRawType().getName().equals("java.lang.Long")
                                || f.getRawType().getName().equals("long"));
                boolean hasStringFieldOnly = javaClass.getAllFields().stream()
                        .anyMatch(f -> f.getRawType().getName().equals("java.lang.String"))
                        && !hasLongField;
                if (hasStringFieldOnly) {
                    return; // UUID(String) ID는 면제
                }
                if (hasLongField) {
                    boolean hasIsNew = javaClass.getAllMethods().stream()
                            .anyMatch(m -> m.getName().equals("isNew"));
                    if (!hasIsNew) {
                        events.add(SimpleConditionEvent.violated(javaClass, String.format(
                                "Long ID VO %s must have isNew() method", javaClass.getName())));
                    }
                }
            }
        };
    }
}
