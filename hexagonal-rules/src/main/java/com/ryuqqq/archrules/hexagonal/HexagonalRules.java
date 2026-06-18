package com.ryuqqq.archrules.hexagonal;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Map;

/** 헥사고날 경계 규칙 — root 패키지 무관(상대 매처). */
public final class HexagonalRules implements ArchRulesService {

    /** 도메인은 프레임워크 비의존(순수 자바). 운영: spring/jakarta/hibernate/jackson.
     *  self-test: 클래스패스에 있는 org.junit 을 프레임워크 프록시로 함께 본다. */
    public static final ArchRule DOMAIN_FRAMEWORK_FREE =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "org.hibernate..",
                            "com.fasterxml.jackson..",
                            "org.junit..")
                    .as("domain is framework-free")
                    .because("도메인은 순수 자바여야 한다(프레임워크 비의존)")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRule> getRules() {
        return Map.of("domain is framework-free", DOMAIN_FRAMEWORK_FREE);
    }
}
