package com.ryuqqq.archrules.security;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

/**
 * 보안 경계 규칙 — root 패키지 무관(상대 매처).
 *
 * <p>주의: {@code public adapter exposes no domain}의 타깃 {@code ..domain..}은 상대 매처라 서드파티 패키지 오인 여지가 있다.
 * base-package 가드 일괄 적용은 후속 과제(cf. context-isolation-rules).
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
     * public 입력 어댑터(외부 OpenAPI)의 DTO는 도메인 타입을 노출하지 않는다.
     */
    public static final ArchRule PUBLIC_ADAPTER_EXPOSES_NO_DOMAIN =
            noClasses().that().resideInAPackage("..adapter.in.publicapi..")
                    .should().dependOnClassesThat().resideInAPackage("..domain..")
                    .as("public adapter exposes no domain")
                    .because("public 입력 어댑터(외부 OpenAPI)의 DTO는 도메인 타입을 노출하지 않는다")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "only gateway depends on authhub", new ArchRuleSpec(ONLY_GATEWAY_DEPENDS_ON_AUTHHUB, Priority.HIGH),
                "public adapter exposes no domain", new ArchRuleSpec(PUBLIC_ADAPTER_EXPOSES_NO_DOMAIN, Priority.HIGH));
    }
}
