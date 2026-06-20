package com.ryuqqq.archrules.security;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.ryuqqq.archrules.common.AppPackages;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Map;

/**
 * 보안 경계 규칙 — root 패키지 무관(상대 매처).
 *
 * <p>public adapter exposes no domain: 앱-베이스 인지 커스텀 조건으로 서드파티 .domain 패키지 오탐 차단.
 */
public final class SecurityBoundaryRules implements ArchRulesService {

    /**
     * authhub 패키지는 gateway 밖에서 의존하지 않는다.
     * 인증은 엣지(gateway)에서만 처리되어야 한다.
     */
    public static final ArchRule ONLY_GATEWAY_DEPENDS_ON_AUTHHUB =
            noClasses().that().resideOutsideOfPackage("..gateway..")
                    .should().dependOnClassesThat().resideInAPackage("..authhub..")
                    .as("only gateway depends on authhub")
                    .because("인증은 엣지(gateway)에서만 — 컨텍스트는 authhub를 의존하지 않는다")
                    .allowEmptyShould(true);

    /**
     * public 입력 어댑터(외부 OpenAPI)의 DTO는 앱 내부 도메인 타입을 노출하지 않는다.
     * 서드파티 .domain 패키지(예: org.springframework.data.domain)는 위반 대상에서 제외.
     */
    public static final ArchRule PUBLIC_ADAPTER_EXPOSES_NO_DOMAIN =
            classes().that().resideInAPackage("..adapter.in.publicapi..")
                    .should(notExposeAppDomain())
                    .as("public adapter exposes no domain")
                    .because("public 입력 어댑터(외부 OpenAPI)의 DTO는 앱 도메인 타입을 노출하지 않는다 (서드파티 .domain 패키지는 제외)")
                    .allowEmptyShould(true);

    private static ArchCondition<JavaClass> notExposeAppDomain() {
        return new ArchCondition<>("앱 내부 도메인 타입을 노출하지 않는다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                for (Dependency dep : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dep.getTargetClass();
                    String pkg = target.getPackageName();
                    boolean inDomain = ("." + pkg + ".").contains(".domain.");
                    if (inDomain && AppPackages.sameApp(origin.getPackageName(), pkg)) {
                        events.add(SimpleConditionEvent.violated(origin,
                                origin.getName() + " → " + target.getName()
                                        + " (public 어댑터가 앱 도메인 타입 노출)"));
                    }
                }
            }
        };
    }

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "only gateway depends on authhub", new ArchRuleSpec(ONLY_GATEWAY_DEPENDS_ON_AUTHHUB, Priority.HIGH),
                "public adapter exposes no domain", new ArchRuleSpec(PUBLIC_ADAPTER_EXPOSES_NO_DOMAIN, Priority.HIGH));
    }
}
