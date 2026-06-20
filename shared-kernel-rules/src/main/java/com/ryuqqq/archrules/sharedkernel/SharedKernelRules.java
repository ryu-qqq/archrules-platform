package com.ryuqqq.archrules.sharedkernel;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

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
import java.util.List;
import java.util.Map;

/**
 * 공유 커널 경계 규칙 — root 패키지 무관(상대 매처).
 *
 * <p>shared-kernel has no reverse dependency: 앱-베이스 인지 커스텀 조건으로 서드파티 .domain/.adapter 등 동명 패키지 오탐 차단.
 */
public final class SharedKernelRules implements ArchRulesService {

    private static final List<String> REVERSE_LAYERS =
            List.of("domain", "application", "adapter", "bootstrap");

    private static ArchCondition<JavaClass> notDependOnAppContextLayers() {
        return new ArchCondition<>("앱 컨텍스트 레이어(domain/application/adapter/bootstrap)를 의존하지 않는다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                for (Dependency dep : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dep.getTargetClass();
                    String pkg = target.getPackageName();
                    if (!AppPackages.sameApp(origin.getPackageName(), pkg)) {
                        continue; // 서드파티 제외
                    }
                    String wrapped = "." + pkg + ".";
                    for (String layer : REVERSE_LAYERS) {
                        if (wrapped.contains("." + layer + ".")) {
                            events.add(SimpleConditionEvent.violated(origin,
                                    origin.getName() + " → " + target.getName()
                                            + " (shared-kernel 역의존: 앱 " + layer + ")"));
                            break;
                        }
                    }
                }
            }
        };
    }

    /**
     * shared-kernel은 domain·application·adapter·bootstrap 등 컨텍스트를 역으로 import하지 않는다.
     * 서드파티 패키지의 동명 세그먼트(예: org.springframework.data.domain)는 위반 대상에서 제외.
     */
    public static final ArchRule SHARED_KERNEL_NO_REVERSE_DEPENDENCY =
            classes().that().resideInAPackage("..shared.kernel..")
                    .should(notDependOnAppContextLayers())
                    .as("shared-kernel has no reverse dependency")
                    .because("shared-kernel은 어떤 앱 컨텍스트도 import하지 않는다(역의존 0; 서드파티 .domain 등 동명 패키지는 제외)")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "shared-kernel has no reverse dependency",
                new ArchRuleSpec(SHARED_KERNEL_NO_REVERSE_DEPENDENCY, Priority.HIGH));
    }
}
