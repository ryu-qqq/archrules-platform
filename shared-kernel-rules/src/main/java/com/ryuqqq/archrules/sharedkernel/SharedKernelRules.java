package com.ryuqqq.archrules.sharedkernel;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

/**
 * 공유 커널 경계 규칙 — root 패키지 무관(상대 매처).
 *
 * <p>주의: 금지 타깃 {@code ..domain..}/{@code ..application..}/{@code ..adapter..}/{@code ..bootstrap..}는 상대 매처라,
 * 전체 클래스패스 실행 시 같은 세그먼트를 가진 서드파티 패키지를 오인할 수 있다(특히 {@code ..adapter..}).
 * base-package 가드 일괄 적용은 후속 과제(cf. context-isolation-rules).
 */
public final class SharedKernelRules implements ArchRulesService {

    /**
     * shared-kernel은 domain·application·adapter·bootstrap 등 컨텍스트를 역으로 import하지 않는다.
     */
    public static final ArchRule SHARED_KERNEL_NO_REVERSE_DEPENDENCY =
            noClasses().that().resideInAPackage("..shared.kernel..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..domain..", "..application..", "..adapter..", "..bootstrap..")
                    .as("shared-kernel has no reverse dependency")
                    .because("shared-kernel은 어떤 컨텍스트도 import하지 않는다(역의존 0)")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "shared-kernel has no reverse dependency",
                new ArchRuleSpec(SHARED_KERNEL_NO_REVERSE_DEPENDENCY, Priority.HIGH));
    }
}
