package com.ryuqqq.archrules.hexagonal;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

/** 헥사고날 경계 규칙 — root 패키지 무관(상대 매처). */
public final class HexagonalRules implements ArchRulesService {

    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String ADAPTER_IN = "..adapter.in..";
    private static final String ADAPTER_OUT = "..adapter.out..";
    private static final String BOOTSTRAP = "..bootstrap..";

    /** 도메인은 프레임워크 비의존. self-test: org.junit 프록시 포함. */
    public static final ArchRule DOMAIN_FRAMEWORK_FREE =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..", "jakarta..", "org.hibernate..",
                            "com.fasterxml.jackson..", "org.junit..",
                            "org.apache.commons..", "com.google.common..", "io.vavr..",
                            "com.google.gson..", "org.slf4j..", "ch.qos.logback..",
                            "org.apache.logging.log4j..", "java.time..")
                    .as("domain is framework-free")
                    .because("도메인은 순수 자바여야 한다(프레임워크·유틸 라이브러리 비의존)")
                    .allowEmptyShould(true);

    /** application은 웹/영속 스택에 직접 의존하지 않는다. self-test: org.junit 프록시 포함. */
    public static final ArchRule APPLICATION_NO_WEB_OR_PERSISTENCE =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..", "jakarta.servlet..",
                            "jakarta.persistence..", "org.hibernate..",
                            "org.springframework.data.jpa..", "com.querydsl..",
                            "org.junit..")
                    .as("application avoids web/persistence")
                    .because("application은 포트로만 바깥과 통신한다(웹/영속 직접 의존 금지)")
                    .allowEmptyShould(true);

    /** 레이어 의존 방향 — 안쪽으로만. */
    public static final ArchRule HEXAGONAL_LAYERS =
            layeredArchitecture().consideringOnlyDependenciesInLayers()
                    .layer("Domain").definedBy(DOMAIN)
                    .layer("Application").definedBy(APPLICATION)
                    .layer("AdapterIn").definedBy(ADAPTER_IN)
                    .layer("AdapterOut").definedBy(ADAPTER_OUT)
                    .layer("Bootstrap").definedBy(BOOTSTRAP)
                    .whereLayer("Bootstrap").mayNotBeAccessedByAnyLayer()
                    .whereLayer("AdapterIn").mayOnlyBeAccessedByLayers("Bootstrap")
                    .whereLayer("AdapterOut").mayOnlyBeAccessedByLayers("Bootstrap")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("AdapterIn", "AdapterOut", "Bootstrap")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "AdapterIn", "AdapterOut", "Bootstrap")
                    .withOptionalLayers(true)
                    .as("hexagonal layer direction")
                    .because("의존은 안쪽으로만: adapter→application→domain, bootstrap만 조립 루트");

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "domain is framework-free", new ArchRuleSpec(DOMAIN_FRAMEWORK_FREE, Priority.HIGH),
                "application avoids web/persistence", new ArchRuleSpec(APPLICATION_NO_WEB_OR_PERSISTENCE, Priority.HIGH),
                "hexagonal layer direction", new ArchRuleSpec(HEXAGONAL_LAYERS, Priority.MEDIUM));
    }
}
