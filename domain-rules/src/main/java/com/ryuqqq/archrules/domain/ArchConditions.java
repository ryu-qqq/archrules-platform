package com.ryuqqq.archrules.domain;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** 카테고리 규칙이 공유하는 ArchCondition 모음(package-private). */
final class ArchConditions {

    private ArchConditions() {}

    /** Java record 여부(java.lang.Record 상속) 검증. */
    static ArchCondition<JavaClass> beRecords() {
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
    static ArchCondition<JavaClass> haveStaticMethodWithName(String methodName) {
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
    static ArchCondition<JavaClass> haveMethodWithName(String methodName) {
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

    /** 특정 접두 메서드 부재 검증. */
    static ArchCondition<JavaClass> notHaveMethodsWithNameStartingWith(String prefix) {
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
