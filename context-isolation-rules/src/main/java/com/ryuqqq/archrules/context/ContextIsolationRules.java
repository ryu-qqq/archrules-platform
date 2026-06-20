package com.ryuqqq.archrules.context;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Map;

/** 컨텍스트 격리 규칙(C-3) — 상대 매처, root 무관. */
public final class ContextIsolationRules implements ArchRulesService {

    private static final DescribedPredicate<JavaClass> BELONGS_TO_CONTEXT =
            new DescribedPredicate<>("컨텍스트에 속한 클래스") {
                @Override
                public boolean test(JavaClass clazz) {
                    return ContextKeys.contextKeyOf(clazz) != null;
                }
            };

    private static final DescribedPredicate<JavaClass> IS_CORE =
            new DescribedPredicate<>("컨텍스트의 domain/application 클래스") {
                @Override
                public boolean test(JavaClass clazz) {
                    String layer = ContextKeys.layerOf(clazz);
                    return "domain".equals(layer) || "application".equals(layer);
                }
            };

    /** 패키지의 상위 2세그먼트(앱 베이스, 예: com.connectly). 세그먼트가 모자라면 그대로 반환. */
    private static String basePackageOf(String packageName) {
        int firstDot = packageName.indexOf('.');
        if (firstDot < 0) {
            return packageName;
        }
        int secondDot = packageName.indexOf('.', firstDot + 1);
        if (secondDot < 0) {
            return packageName;
        }
        return packageName.substring(0, secondDot);
    }

    private static ArchCondition<JavaClass> notDependOnOtherContextInternals() {
        return new ArchCondition<>("다른 컨텍스트의 domain/application/internal을 직접 의존하지 않는다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String originCtx = ContextKeys.contextKeyOf(origin);
                if (originCtx == null) {
                    return;
                }
                String originBase = basePackageOf(originCtx);
                for (Dependency dep : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dep.getTargetClass();
                    String targetCtx = ContextKeys.contextKeyOf(target);
                    if (targetCtx == null || targetCtx.equals(originCtx)
                            || !targetCtx.startsWith(originBase + ".")) {
                        continue;
                    }
                    String layer = ContextKeys.layerOf(target);
                    if ("domain".equals(layer) || "application".equals(layer) || "internal".equals(layer)) {
                        events.add(SimpleConditionEvent.violated(origin,
                                origin.getName() + " → " + target.getName()
                                        + " (다른 컨텍스트 " + layer + " 직접 의존)"));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notDependOnOtherContextApi() {
        return new ArchCondition<>("다른 컨텍스트의 .api를 직접 의존하지 않는다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String originCtx = ContextKeys.contextKeyOf(origin);
                if (originCtx == null) {
                    return;
                }
                String originBase = basePackageOf(originCtx);
                for (Dependency dep : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dep.getTargetClass();
                    String targetCtx = ContextKeys.contextKeyOf(target);
                    if (targetCtx == null || targetCtx.equals(originCtx)
                            || !targetCtx.startsWith(originBase + ".")) {
                        continue;
                    }
                    if ("api".equals(ContextKeys.layerOf(target))) {
                        events.add(SimpleConditionEvent.violated(origin,
                                origin.getName() + " → " + target.getName()
                                        + " (코어가 다른 컨텍스트 .api 직접 의존)"));
                    }
                }
            }
        };
    }

    public static final ArchRule NO_CROSS_CONTEXT_INTERNALS =
            classes().that(BELONGS_TO_CONTEXT)
                    .should(notDependOnOtherContextInternals())
                    .as("no cross-context internals")
                    .because("컨텍스트는 다른 컨텍스트의 domain/application/internal을 직접 의존하지 않는다 (교차는 .api/이벤트로만)")
                    .allowEmptyShould(true);

    public static final ArchRule CORE_BLIND_TO_FOREIGN_API =
            classes().that(IS_CORE)
                    .should(notDependOnOtherContextApi())
                    .as("core blind to foreign api")
                    .because("컨텍스트의 domain/application은 다른 컨텍스트의 .api조차 직접 의존하지 않는다 (자기 포트를 정의하고, 교차 의존은 어댑터에서만)")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "no cross-context internals",
                new ArchRuleSpec(NO_CROSS_CONTEXT_INTERNALS, Priority.HIGH),
                "core blind to foreign api",
                new ArchRuleSpec(CORE_BLIND_TO_FOREIGN_API, Priority.HIGH));
    }
}
