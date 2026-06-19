package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;
import java.util.Set;

/** 도메인 작성 컨벤션 규칙 — 상대 매처(..domain..)로 root 무관. */
public final class DomainRules implements ArchRulesService {

    private static final String DOMAIN = "..domain..";

    /** 현재 시각 직접 읽기 호출(now/systemUTC/currentTimeMillis 등). */
    private static final DescribedPredicate<JavaMethodCall> TIME_NOW_CALL =
            new DescribedPredicate<>("현재 시각을 직접 읽는 호출") {
                private final Map<String, Set<String>> banned = Map.of(
                        "java.time.Instant", Set.of("now"),
                        "java.time.LocalDateTime", Set.of("now"),
                        "java.time.LocalDate", Set.of("now"),
                        "java.time.LocalTime", Set.of("now"),
                        "java.time.ZonedDateTime", Set.of("now"),
                        "java.time.OffsetDateTime", Set.of("now"),
                        "java.time.Clock", Set.of("systemUTC", "systemDefaultZone", "system"),
                        "java.lang.System", Set.of("currentTimeMillis", "nanoTime"));

                @Override
                public boolean test(JavaMethodCall call) {
                    Set<String> names = banned.get(call.getTargetOwner().getFullName());
                    return names != null && names.contains(call.getName());
                }
            };

    public static final ArchRule NO_TIME_IN_DOMAIN =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().callMethodWhere(TIME_NOW_CALL)
                    .as("domain reads no clock")
                    .because("도메인은 현재 시각을 직접 읽지 않고 주입받는다")
                    .allowEmptyShould(true);

    public static final ArchRule NO_SETTERS_IN_DOMAIN =
            noMethods().that().areDeclaredInClassesThat().resideInAPackage(DOMAIN)
                    .should().haveNameStartingWith("set")
                    .as("domain has no setters")
                    .because("도메인 상태 변경은 set* 가 아니라 비즈니스 메서드로만 한다")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "domain reads no clock", new ArchRuleSpec(NO_TIME_IN_DOMAIN, Priority.HIGH),
                "domain has no setters", new ArchRuleSpec(NO_SETTERS_IN_DOMAIN, Priority.MEDIUM),
                "domain VO is record", new ArchRuleSpec(VoRules.VO_IS_RECORD, Priority.HIGH),
                "domain VO has static factory of", new ArchRuleSpec(VoRules.VO_HAS_OF, Priority.HIGH),
                "domain VO has no create method", new ArchRuleSpec(VoRules.VO_NO_CREATE, Priority.MEDIUM));
    }
}
