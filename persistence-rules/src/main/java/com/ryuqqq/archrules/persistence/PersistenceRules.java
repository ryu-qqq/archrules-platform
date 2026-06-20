package com.ryuqqq.archrules.persistence;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

/**
 * 영속 어댑터 경계 규칙 — root 패키지 무관(상대 매처).
 *
 * <p>영속 어댑터의 '자기 스키마 테이블만 접근'은 런타임 특성이라 정적 규칙이 아닌 review-gate(🟡)로 둔다.
 */
public final class PersistenceRules implements ArchRulesService {

    /**
     * JPA 엔티티는 영속 어댑터(persistence..) 및 아웃바운드 어댑터(adapter.out..) 밖으로 새지 않는다.
     * 엔티티를 의존하는 클래스는 반드시 persistence 패키지 또는 adapter.out 패키지 안에만 있어야 한다.
     */
    public static final ArchRule JPA_ENTITY_DOES_NOT_LEAK =
            classes().that().resideInAPackage("..persistence.entity..")
                    .should().onlyHaveDependentClassesThat()
                    .resideInAnyPackage("..persistence..", "..adapter.out..")
                    .as("jpa entity does not leak outside persistence")
                    .because("JPA 엔티티는 영속 어댑터 밖으로 새지 않는다(도메인=엔티티 분리)")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "jpa entity does not leak outside persistence",
                new ArchRuleSpec(JPA_ENTITY_DOES_NOT_LEAK, Priority.HIGH));
    }
}
