package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Map;

/** Exception 작성 컨벤션 — 상대 매처 ..exception.. 로 root 무관. */
final class ExceptionRules {

    private ExceptionRules() {}

    private static final String EXCEPTION_PKG = "..exception..";

    // ==================== ErrorCode Enum 규칙 ====================

    /** 규칙 #1: ErrorCode enum이 ErrorCode 인터페이스를 구현해야 한다 */
    static final ArchRule ERRORCODE_IMPLEMENTS_INTERFACE =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("ErrorCode")
                    .and().areEnums()
                    .should(implementErrorCodeInterface())
                    .as("errorcode implements ErrorCode interface")
                    .because("ErrorCode Enum은 ErrorCode 인터페이스를 구현해야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #2: ErrorCode enum이 ..exception.. 패키지에 위치해야 한다 */
    static final ArchRule ERRORCODE_IN_EXCEPTION_PKG =
            classes().that()
                    .haveSimpleNameEndingWith("ErrorCode")
                    .and().areEnums()
                    .should().resideInAPackage(EXCEPTION_PKG)
                    .as("errorcode resides in exception package")
                    .because("ErrorCode Enum은 exception 패키지에 위치해야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #4: ErrorCode enum이 public이어야 한다 */
    static final ArchRule ERRORCODE_IS_PUBLIC =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("ErrorCode")
                    .and().areEnums()
                    .should().bePublic()
                    .as("errorcode is public")
                    .because("ErrorCode Enum은 다른 레이어에서 사용되기 위해 public이어야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #5: ErrorCode enum이 getCode() 메서드를 가져야 한다 */
    static final ArchRule ERRORCODE_HAS_GET_CODE =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("ErrorCode")
                    .and().areEnums()
                    .should(ArchConditions.haveMethodWithName("getCode"))
                    .as("errorcode has getCode")
                    .because("ErrorCode Enum은 getCode() 메서드를 구현해야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #6: ErrorCode enum이 getHttpStatus() 메서드를 가져야 한다 */
    static final ArchRule ERRORCODE_HAS_GET_HTTP_STATUS =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("ErrorCode")
                    .and().areEnums()
                    .should(ArchConditions.haveMethodWithName("getHttpStatus"))
                    .as("errorcode has getHttpStatus")
                    .because("ErrorCode Enum은 getHttpStatus() 메서드를 구현해야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #7: ErrorCode enum이 getMessage() 메서드를 가져야 한다 */
    static final ArchRule ERRORCODE_HAS_GET_MESSAGE =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("ErrorCode")
                    .and().areEnums()
                    .should(ArchConditions.haveMethodWithName("getMessage"))
                    .as("errorcode has getMessage")
                    .because("ErrorCode Enum은 getMessage() 메서드를 구현해야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #8: ErrorCode enum의 getHttpStatus()가 Spring HttpStatus 타입을 반환하지 않아야 한다 */
    static final ArchRule ERRORCODE_GET_HTTP_STATUS_NON_SPRING =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("ErrorCode")
                    .and().areEnums()
                    .should(haveGetHttpStatusWithValidReturnType())
                    .as("errorcode getHttpStatus returns non-spring")
                    .because("ErrorCode Enum의 getHttpStatus()는 int 또는 적절한 타입을 반환해야 합니다 (Spring HttpStatus 의존 금지)")
                    .allowEmptyShould(true);

    // ==================== Concrete Exception 클래스 규칙 ====================

    /** 규칙 #9: Concrete Exception이 DomainException을 상속해야 한다 */
    static final ArchRule EXCEPTION_EXTENDS_DOMAIN_EXCEPTION =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("Exception")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotInterfaces()
                    .and().doNotHaveSimpleName("DomainException")
                    .should(extendDomainException())
                    .as("exception extends DomainException")
                    .because("Concrete Exception 클래스는 DomainException을 상속해야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #10: Concrete Exception이 ..exception.. 패키지에 위치해야 한다 */
    static final ArchRule EXCEPTION_IN_EXCEPTION_PKG =
            classes().that()
                    .haveSimpleNameEndingWith("Exception")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotInterfaces()
                    .and().doNotHaveSimpleName("DomainException")
                    .and().resideInAPackage("..domain..")
                    .should().resideInAPackage(EXCEPTION_PKG)
                    .as("exception resides in exception package")
                    .because("Concrete Exception 클래스는 exception 패키지에 위치해야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #14: Concrete Exception이 public이어야 한다 */
    static final ArchRule EXCEPTION_IS_PUBLIC =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("Exception")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotInterfaces()
                    .and().doNotHaveSimpleName("DomainException")
                    .should().bePublic()
                    .as("exception is public")
                    .because("Concrete Exception 클래스는 다른 레이어에서 사용되기 위해 public이어야 합니다")
                    .allowEmptyShould(true);

    /** 규칙 #15: Concrete Exception이 RuntimeException을 상속해야 한다 */
    static final ArchRule EXCEPTION_EXTENDS_RUNTIME_EXCEPTION =
            classes().that().resideInAPackage(EXCEPTION_PKG)
                    .and().haveSimpleNameEndingWith("Exception")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotInterfaces()
                    .and().doNotHaveSimpleName("DomainException")
                    .should().beAssignableTo(RuntimeException.class)
                    .as("exception extends RuntimeException")
                    .because("Concrete Exception 클래스는 RuntimeException을 상속해야 합니다 (Checked Exception 금지)")
                    .allowEmptyShould(true);

    /** 규칙 #18: exception 레이어가 외부 레이어(application/adapter)에 의존하지 않아야 한다 */
    static final ArchRule EXCEPTION_NO_OUTER_LAYER_DEPS =
            noClasses().that().resideInAPackage(EXCEPTION_PKG)
                    .should().dependOnClassesThat().resideInAnyPackage("..application..", "..adapter..")
                    .as("exception does not depend on outer layers")
                    .because("Domain Exception은 Application/Adapter 레이어에 의존하지 않아야 합니다 (헥사고날 아키텍처)")
                    .allowEmptyShould(true);

    // ==================== rules() 맵 ====================

    static Map<String, ArchRuleSpec> rules() {
        return Map.ofEntries(
                Map.entry("errorcode implements ErrorCode interface",
                        new ArchRuleSpec(ERRORCODE_IMPLEMENTS_INTERFACE, Priority.HIGH)),
                Map.entry("errorcode resides in exception package",
                        new ArchRuleSpec(ERRORCODE_IN_EXCEPTION_PKG, Priority.MEDIUM)),
                Map.entry("errorcode is public",
                        new ArchRuleSpec(ERRORCODE_IS_PUBLIC, Priority.MEDIUM)),
                Map.entry("errorcode has getCode",
                        new ArchRuleSpec(ERRORCODE_HAS_GET_CODE, Priority.HIGH)),
                Map.entry("errorcode has getHttpStatus",
                        new ArchRuleSpec(ERRORCODE_HAS_GET_HTTP_STATUS, Priority.HIGH)),
                Map.entry("errorcode has getMessage",
                        new ArchRuleSpec(ERRORCODE_HAS_GET_MESSAGE, Priority.HIGH)),
                Map.entry("errorcode getHttpStatus returns non-spring",
                        new ArchRuleSpec(ERRORCODE_GET_HTTP_STATUS_NON_SPRING, Priority.HIGH)),
                Map.entry("exception extends DomainException",
                        new ArchRuleSpec(EXCEPTION_EXTENDS_DOMAIN_EXCEPTION, Priority.HIGH)),
                Map.entry("exception resides in exception package",
                        new ArchRuleSpec(EXCEPTION_IN_EXCEPTION_PKG, Priority.MEDIUM)),
                Map.entry("exception is public",
                        new ArchRuleSpec(EXCEPTION_IS_PUBLIC, Priority.MEDIUM)),
                Map.entry("exception extends RuntimeException",
                        new ArchRuleSpec(EXCEPTION_EXTENDS_RUNTIME_EXCEPTION, Priority.HIGH)),
                Map.entry("exception does not depend on outer layers",
                        new ArchRuleSpec(EXCEPTION_NO_OUTER_LAYER_DEPS, Priority.HIGH))
        );
    }

    // ==================== private static ArchCondition 헬퍼 ====================

    /** ErrorCode 인터페이스를 구현하는지 검증 (이름 기반 비교) */
    private static ArchCondition<JavaClass> implementErrorCodeInterface() {
        return new ArchCondition<JavaClass>("implement ErrorCode interface") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean implementsErrorCode =
                        javaClass.getAllRawInterfaces().stream()
                                .anyMatch(iface -> iface.getSimpleName().equals("ErrorCode"));

                if (!implementsErrorCode) {
                    String message =
                            String.format(
                                    "Class %s does not implement ErrorCode interface",
                                    javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /** getHttpStatus() 메서드가 Spring 타입을 반환하지 않는지 검증 */
    private static ArchCondition<JavaClass> haveGetHttpStatusWithValidReturnType() {
        return new ArchCondition<JavaClass>("have getHttpStatus() method with valid return type") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasValidMethod =
                        javaClass.getAllMethods().stream()
                                .filter(method -> method.getName().equals("getHttpStatus"))
                                .anyMatch(
                                        method -> {
                                            String returnType = method.getRawReturnType().getName();
                                            return !returnType.startsWith("org.springframework");
                                        });

                if (!hasValidMethod) {
                    String message =
                            String.format(
                                    "Class %s's getHttpStatus() method should return int or"
                                            + " non-Spring type (not"
                                            + " org.springframework.http.HttpStatus)",
                                    javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /** DomainException을 상속하는지 검증 (이름 기반 비교) */
    private static ArchCondition<JavaClass> extendDomainException() {
        return new ArchCondition<JavaClass>("extend DomainException") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean extendsDomainException =
                        javaClass.getAllRawSuperclasses().stream()
                                .anyMatch(
                                        superClass ->
                                                superClass
                                                        .getSimpleName()
                                                        .equals("DomainException"));

                if (!extendsDomainException) {
                    String message =
                            String.format(
                                    "Class %s does not extend DomainException",
                                    javaClass.getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }
}
