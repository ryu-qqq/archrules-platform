package com.ryuqqq.archrules.messaging;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

/**
 * 메시징(이벤트) 경계 규칙 — root 패키지 무관(상대 매처).
 *
 * <p>정적 강제 범위:
 * <ul>
 *   <li>🟡 이벤트 payload는 도메인 애그리거트를 노출하지 않는다 (여기서 강제).</li>
 * </ul>
 *
 * <p>review-gate(🔵)로 남기는 항목 — 런타임/구조 특성이라 정적 규칙이 아님:
 * <ul>
 *   <li>transactional outbox (애그리거트 + outbox 한 tx)</li>
 *   <li>멱등 consumer (inbox)</li>
 *   <li>EventEnvelope 감싸기</li>
 * </ul>
 */
public final class MessagingRules implements ArchRulesService {

    /**
     * 이벤트 payload는 도메인 애그리거트가 아니라 published DTO다.
     * payload가 aggregate를 직접 참조하면 내부 리팩터가 구독자(downstream)를 깨뜨린다.
     */
    public static final ArchRule EVENT_PAYLOAD_EXPOSES_NO_AGGREGATE =
            noClasses().that().resideInAPackage("..event.payload..")
                    .should().dependOnClassesThat().resideInAPackage("..aggregate..")
                    .as("event payload exposes no aggregate")
                    .because("이벤트 payload는 도메인 애그리거트가 아니라 published DTO다(내부 리팩터가 구독자를 깨면 안 됨)")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "event payload exposes no aggregate",
                new ArchRuleSpec(EVENT_PAYLOAD_EXPOSES_NO_AGGREGATE, Priority.MEDIUM));
    }
}
