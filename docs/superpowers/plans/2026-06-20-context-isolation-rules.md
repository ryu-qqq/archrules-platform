# context-isolation-rules 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** connectly-services 설계의 핵심 규칙 C-3(컨텍스트 격리)을 강제하는 신규 ArchUnit 규칙 라이브러리 모듈 `context-isolation-rules`를 추가한다 — "어떤 컨텍스트도 다른 컨텍스트의 domain/application/internal을 직접 의존하지 않고, 교차는 `.api`/이벤트로만" 을 컴파일 검증으로 만든다.

**Architecture:** 기존 `archrules-platform`의 규칙 라이브러리 패턴(`ArchRulesService` 구현 + `Map<String, ArchRuleSpec>` 노출 + ServiceLoader 등록 + `Runner.check` self-test)을 그대로 따른다. 컨텍스트 경계는 패키지 세그먼트(`.domain`/`.application`/`.api`/`.adapter`/`.internal`) 직전까지를 "컨텍스트 키"로 추출하는 순수 함수 + 커스텀 `ArchCondition`으로 표현한다(상대 매처라 root 패키지 무관).

**Tech Stack:** Java, ArchUnit 1.x, JUnit 5, Gradle 멀티모듈. 의존은 `api project(':archrules-api')`(archunit 전이 노출) + 테스트에 `archrules-runtime`.

## Global Constraints

- 모든 규칙은 **상대 매처**(`..domain..` 식)로 작성 — root 패키지 무관.
- 모든 규칙은 `.as(name)` + `.because(사유)` + `Priority` + `.allowEmptyShould(true)` 를 갖춘다.
- 규칙 노출은 `ArchRulesService.getRules()` → `Map<String, ArchRuleSpec>`. 키 = 규칙 이름(=`.as()` 값과 동일).
- self-test는 `com.ryuqqq.archrules.runtime.Runner.check(rule, Class...).hasViolation()` 으로 픽스처 클래스에 직접 평가.
- 신규 규칙 라이브러리는 `src/main/resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService` 에 구현 클래스 FQN 1줄 등록.
- 컨텍스트 키 추출 마커(세그먼트 경계): `.domain` `.application` `.api` `.adapter` `.internal`. shared-kernel 등 공용 패키지는 이 마커 세그먼트를 쓰지 않는다(컨벤션).
- 신규 모듈 패키지 루트: `com.ryuqqq.archrules.context`. 모듈 디렉토리: `context-isolation-rules`.

---

### Task 1: 모듈 스캐폴딩 + 컨텍스트 키 추출 순수 함수

**Files:**
- Modify: `settings.gradle`
- Create: `context-isolation-rules/build.gradle`
- Create: `context-isolation-rules/src/main/java/com/ryuqqq/archrules/context/ContextKeys.java`
- Test: `context-isolation-rules/src/test/java/com/ryuqqq/archrules/context/ContextKeysTest.java`

**Interfaces:**
- Produces: `ContextKeys.contextKeyOf(String) -> String|null`, `ContextKeys.layerOf(String) -> String|null` ("domain"/"application"/"api"/"adapter"/"internal" 또는 null). 이후 Task 2/3 커스텀 condition이 사용.

- [ ] **Step 1: settings.gradle에 모듈 등록** — 마지막 `include` 줄 아래에 `include 'context-isolation-rules'` 추가.

- [ ] **Step 2: build.gradle 작성** — `context-isolation-rules/build.gradle`:

```gradle
dependencies {
    api project(':archrules-api')
    testImplementation project(':archrules-runtime')
    testImplementation platform(libs.junit.bom)
    testImplementation libs.junit.jupiter
    testImplementation libs.archunit
}
```

- [ ] **Step 3: 실패하는 테스트 작성** — `ContextKeysTest.java`:

```java
package com.ryuqqq.archrules.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ContextKeysTest {

    @Test
    void contextKeyIsPackageBeforeLayerMarker() {
        assertEquals("com.x.marketplace.product",
                ContextKeys.contextKeyOf("com.x.marketplace.product.application.service"));
        assertEquals("com.x.marketplace.order",
                ContextKeys.contextKeyOf("com.x.marketplace.order.domain.aggregate"));
    }

    @Test
    void contextKeyUsesEarliestMarker() {
        assertEquals("com.x.alpha",
                ContextKeys.contextKeyOf("com.x.alpha.application.api.dto"));
    }

    @Test
    void nonContextPackageHasNoKey() {
        assertNull(ContextKeys.contextKeyOf("com.x.shared.vo"));
        assertNull(ContextKeys.contextKeyOf("java.time"));
    }

    @Test
    void markerMustBeWholeSegment() {
        assertNull(ContextKeys.contextKeyOf("com.x.apination.stuff"));
    }

    @Test
    void layerOfReturnsLayerName() {
        assertEquals("application", ContextKeys.layerOf("com.x.alpha.application.svc"));
        assertEquals("domain", ContextKeys.layerOf("com.x.alpha.domain.agg"));
        assertEquals("api", ContextKeys.layerOf("com.x.alpha.api.port"));
        assertNull(ContextKeys.layerOf("com.x.shared.vo"));
    }
}
```

- [ ] **Step 4: 테스트 실패 확인** — Run: `./gradlew :context-isolation-rules:test --tests '*ContextKeysTest'` / Expected: 컴파일 실패(`ContextKeys` 없음).

- [ ] **Step 5: ContextKeys 구현** — `ContextKeys.java`:

```java
package com.ryuqqq.archrules.context;

import com.tngtech.archunit.core.domain.JavaClass;
import java.util.List;

/** 패키지명에서 컨텍스트 경계를 식별 — 레이어 마커 세그먼트 직전까지가 "컨텍스트 키". */
final class ContextKeys {

    private ContextKeys() {}

    static final List<String> LAYER_MARKERS =
            List.of(".domain", ".application", ".api", ".adapter", ".internal");

    /** 레이어 마커 직전까지의 패키지. 컨텍스트 클래스가 아니면 null. */
    static String contextKeyOf(String packageName) {
        int best = -1;
        for (String marker : LAYER_MARKERS) {
            int idx = segmentIndex(packageName, marker);
            if (idx >= 0 && (best < 0 || idx < best)) {
                best = idx;
            }
        }
        return best < 0 ? null : packageName.substring(0, best);
    }

    /** 레이어 종류("domain"/"application"/...) 또는 null. */
    static String layerOf(String packageName) {
        String key = contextKeyOf(packageName);
        if (key == null) {
            return null;
        }
        String rest = packageName.substring(key.length()); // 예: ".application.svc"
        for (String marker : LAYER_MARKERS) {
            if (rest.equals(marker) || rest.startsWith(marker + ".")) {
                return marker.substring(1);
            }
        }
        return null;
    }

    static String contextKeyOf(JavaClass clazz) {
        return contextKeyOf(clazz.getPackageName());
    }

    static String layerOf(JavaClass clazz) {
        return layerOf(clazz.getPackageName());
    }

    /** marker가 세그먼트 경계(뒤가 끝이거나 '.')로 등장하는 첫 위치. 없으면 -1. */
    private static int segmentIndex(String pkg, String marker) {
        int from = 0;
        while (true) {
            int idx = pkg.indexOf(marker, from);
            if (idx < 0) {
                return -1;
            }
            int end = idx + marker.length();
            boolean segmentBoundary = end == pkg.length() || pkg.charAt(end) == '.';
            if (segmentBoundary) {
                return idx;
            }
            from = end;
        }
    }
}
```

- [ ] **Step 6: 테스트 통과 확인** — Run: `./gradlew :context-isolation-rules:test --tests '*ContextKeysTest'` / Expected: PASS (5).

- [ ] **Step 7: 커밋**

```bash
git add settings.gradle context-isolation-rules/build.gradle \
        context-isolation-rules/src/main/java/com/ryuqqq/archrules/context/ContextKeys.java \
        context-isolation-rules/src/test/java/com/ryuqqq/archrules/context/ContextKeysTest.java
git commit -m "feat(context-isolation-rules): 모듈 스캐폴딩 + 컨텍스트 키 추출(ContextKeys)

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 2: R1 — 다른 컨텍스트 internals 직접 의존 금지

**Files:**
- Create: `context-isolation-rules/src/main/java/com/ryuqqq/archrules/context/ContextIsolationRules.java`
- Create: `context-isolation-rules/src/main/resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService`
- Create (fixtures): `.../test/.../fixture/compliant/alpha/{application/AlphaService, domain/AlphaAggregate, adapter/AlphaToBetaBridge}.java`, `.../compliant/beta/api/BetaUseCase.java`, `.../violation/alpha/application/LeakyAlphaService.java`, `.../violation/beta/domain/BetaAggregate.java`
- Test: `.../ContextIsolationRulesTest.java`, `.../RuleConventionTest.java`

**Interfaces:**
- Consumes: `ContextKeys.contextKeyOf/layerOf` (Task 1).
- Produces: `ContextIsolationRules implements ArchRulesService`; 규칙 키 `"no cross-context internals"`.

- [ ] **Step 1: 실패하는 self-test 작성** — `ContextIsolationRulesTest.java`:

```java
package com.ryuqqq.archrules.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.context.fixture.compliant.alpha.adapter.AlphaToBetaBridge;
import com.ryuqqq.archrules.context.fixture.compliant.alpha.application.AlphaService;
import com.ryuqqq.archrules.context.fixture.compliant.alpha.domain.AlphaAggregate;
import com.ryuqqq.archrules.context.fixture.compliant.beta.api.BetaUseCase;
import com.ryuqqq.archrules.context.fixture.violation.alpha.application.LeakyAlphaService;
import com.ryuqqq.archrules.context.fixture.violation.beta.domain.BetaAggregate;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ContextIsolationRulesTest {

    private final ArchRule noCrossInternals =
            new ContextIsolationRules().getRules().get("no cross-context internals").rule();

    @Test
    void ruleIsExposed() {
        assertNotNull(noCrossInternals);
    }

    @Test
    void sameContextApplicationToDomainPasses() {
        assertFalse(Runner.check(noCrossInternals, AlphaService.class, AlphaAggregate.class)
                .hasViolation(), "같은 컨텍스트 application→domain 은 허용");
    }

    @Test
    void bridgeAdapterToForeignApiPasses() {
        assertFalse(Runner.check(noCrossInternals, AlphaToBetaBridge.class, BetaUseCase.class)
                .hasViolation(), "어댑터→다른 컨텍스트 .api 는 허용(교차 seam)");
    }

    @Test
    void applicationToForeignDomainViolates() {
        assertTrue(Runner.check(noCrossInternals, LeakyAlphaService.class, BetaAggregate.class)
                .hasViolation(), "application→다른 컨텍스트 domain 은 위반");
    }
}
```

- [ ] **Step 2: 픽스처 작성**

`.../fixture/compliant/alpha/domain/AlphaAggregate.java`:
```java
package com.ryuqqq.archrules.context.fixture.compliant.alpha.domain;

public final class AlphaAggregate {
    private final String id;
    private AlphaAggregate(String id) { this.id = id; }
    public static AlphaAggregate of(String id) { return new AlphaAggregate(id); }
    public String id() { return id; }
}
```

`.../fixture/compliant/alpha/application/AlphaService.java`:
```java
package com.ryuqqq.archrules.context.fixture.compliant.alpha.application;

import com.ryuqqq.archrules.context.fixture.compliant.alpha.domain.AlphaAggregate;

public final class AlphaService {
    public String handle(String id) { return AlphaAggregate.of(id).id(); }
}
```

`.../fixture/compliant/beta/api/BetaUseCase.java`:
```java
package com.ryuqqq.archrules.context.fixture.compliant.beta.api;

public interface BetaUseCase {
    String describe(String key);
}
```

`.../fixture/compliant/alpha/adapter/AlphaToBetaBridge.java`:
```java
package com.ryuqqq.archrules.context.fixture.compliant.alpha.adapter;

import com.ryuqqq.archrules.context.fixture.compliant.beta.api.BetaUseCase;

public final class AlphaToBetaBridge {
    private final BetaUseCase betaUseCase;
    public AlphaToBetaBridge(BetaUseCase betaUseCase) { this.betaUseCase = betaUseCase; }
    public String bridge(String key) { return betaUseCase.describe(key); }
}
```

`.../fixture/violation/beta/domain/BetaAggregate.java`:
```java
package com.ryuqqq.archrules.context.fixture.violation.beta.domain;

public final class BetaAggregate {
    public String value() { return "beta"; }
}
```

`.../fixture/violation/alpha/application/LeakyAlphaService.java`:
```java
package com.ryuqqq.archrules.context.fixture.violation.alpha.application;

import com.ryuqqq.archrules.context.fixture.violation.beta.domain.BetaAggregate;

public final class LeakyAlphaService {
    private final BetaAggregate beta = new BetaAggregate();
    public String leak() { return beta.value(); }
}
```

- [ ] **Step 3: 테스트 실패 확인** — Run: `./gradlew :context-isolation-rules:test --tests '*ContextIsolationRulesTest'` / Expected: 컴파일 실패(`ContextIsolationRules` 없음).

- [ ] **Step 4: ContextIsolationRules 구현 (R1)** — `ContextIsolationRules.java`:

```java
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

    private static ArchCondition<JavaClass> notDependOnOtherContextInternals() {
        return new ArchCondition<>("다른 컨텍스트의 domain/application/internal을 직접 의존하지 않는다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String originCtx = ContextKeys.contextKeyOf(origin);
                if (originCtx == null) {
                    return;
                }
                for (Dependency dep : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dep.getTargetClass();
                    String targetCtx = ContextKeys.contextKeyOf(target);
                    if (targetCtx == null || targetCtx.equals(originCtx)) {
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

    public static final ArchRule NO_CROSS_CONTEXT_INTERNALS =
            classes().that(BELONGS_TO_CONTEXT)
                    .should(notDependOnOtherContextInternals())
                    .as("no cross-context internals")
                    .because("컨텍스트는 다른 컨텍스트의 domain/application/internal을 직접 의존하지 않는다 (교차는 .api/이벤트로만)")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "no cross-context internals",
                new ArchRuleSpec(NO_CROSS_CONTEXT_INTERNALS, Priority.HIGH));
    }
}
```

- [ ] **Step 5: ServiceLoader 등록** — `context-isolation-rules/src/main/resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService` 내용 1줄:

```
com.ryuqqq.archrules.context.ContextIsolationRules
```

- [ ] **Step 6: 테스트 통과 확인** — Run: `./gradlew :context-isolation-rules:test --tests '*ContextIsolationRulesTest'` / Expected: PASS (4).

- [ ] **Step 7: RuleConventionTest 추가** — `RuleConventionTest.java`:

```java
package com.ryuqqq.archrules.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuleConventionTest {

    private final Map<String, ArchRuleSpec> rules = new ContextIsolationRules().getRules();

    @Test
    void everyRuleHasPriorityAndBecause() {
        assertFalse(rules.isEmpty(), "규칙이 비어있지 않다");
        rules.forEach((name, spec) -> {
            assertNotNull(spec.priority(), name + " priority non-null");
            String desc = spec.rule().getDescription();
            assertTrue(desc != null && desc.contains("because"),
                    name + " 규칙은 .because() 사유를 가진다: " + desc);
        });
    }
}
```

- [ ] **Step 8: 전체 모듈 테스트 + 커밋** — Run: `./gradlew :context-isolation-rules:test` / Expected: PASS.

```bash
git add context-isolation-rules/src/main context-isolation-rules/src/test
git commit -m "feat(context-isolation-rules): R1 다른 컨텍스트 internals 직접 의존 금지 + 픽스처/self-test

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 3: R2 — 코어(domain/application)는 다른 컨텍스트 .api도 직접 의존 금지

**Files:**
- Modify: `context-isolation-rules/src/main/java/com/ryuqqq/archrules/context/ContextIsolationRules.java`
- Create (fixtures): `.../fixture/violation/gamma/application/GammaUsesForeignApi.java`, `.../fixture/violation/delta/api/DeltaUseCase.java`
- Modify (test): `.../ContextIsolationRulesTest.java`

**Interfaces:**
- Consumes: `ContextKeys`, Task 2의 `ContextIsolationRules`.
- Produces: 규칙 키 `"core blind to foreign api"`.

근거: 설계 D5/§3 — order-application은 product-api조차 의존하지 않고 자기 포트를 정의한다. 다른 컨텍스트 `.api` 의존은 어댑터(다리)에서만.

- [ ] **Step 1: 실패하는 self-test 추가** — `ContextIsolationRulesTest.java` 에 import 2줄과 테스트 2개 추가:

```java
import com.ryuqqq.archrules.context.fixture.violation.delta.api.DeltaUseCase;
import com.ryuqqq.archrules.context.fixture.violation.gamma.application.GammaUsesForeignApi;
```

```java
    @Test
    void coreToForeignApiViolates() {
        ArchRule rule = new ContextIsolationRules().getRules().get("core blind to foreign api").rule();
        assertTrue(Runner.check(rule, GammaUsesForeignApi.class, DeltaUseCase.class)
                .hasViolation(), "application→다른 컨텍스트 .api 는 위반(코어는 자기 포트만)");
    }

    @Test
    void bridgeAdapterToForeignApiPassesCoreRule() {
        ArchRule rule = new ContextIsolationRules().getRules().get("core blind to foreign api").rule();
        assertFalse(Runner.check(rule, AlphaToBetaBridge.class, BetaUseCase.class)
                .hasViolation(), "어댑터는 다른 컨텍스트 .api 의존 허용");
    }
```

- [ ] **Step 2: 픽스처 작성**

`.../fixture/violation/delta/api/DeltaUseCase.java`:
```java
package com.ryuqqq.archrules.context.fixture.violation.delta.api;

public interface DeltaUseCase {
    String describe();
}
```

`.../fixture/violation/gamma/application/GammaUsesForeignApi.java`:
```java
package com.ryuqqq.archrules.context.fixture.violation.gamma.application;

import com.ryuqqq.archrules.context.fixture.violation.delta.api.DeltaUseCase;

public final class GammaUsesForeignApi {
    private final DeltaUseCase delta;
    public GammaUsesForeignApi(DeltaUseCase delta) { this.delta = delta; }
    public String use() { return delta.describe(); }
}
```

- [ ] **Step 3: 테스트 실패 확인** — Run: `./gradlew :context-isolation-rules:test --tests '*ContextIsolationRulesTest'` / Expected: FAIL — `"core blind to foreign api"` 규칙이 없어 `get(...)` null.

- [ ] **Step 4: R2 규칙 구현 (ContextIsolationRules 수정)**

`BELONGS_TO_CONTEXT` 아래에 코어 판별 predicate 추가:
```java
    private static final DescribedPredicate<JavaClass> IS_CORE =
            new DescribedPredicate<>("컨텍스트의 domain/application 클래스") {
                @Override
                public boolean test(JavaClass clazz) {
                    String layer = ContextKeys.layerOf(clazz);
                    return "domain".equals(layer) || "application".equals(layer);
                }
            };
```

`notDependOnOtherContextInternals()` 아래에 condition 추가:
```java
    private static ArchCondition<JavaClass> notDependOnOtherContextApi() {
        return new ArchCondition<>("다른 컨텍스트의 .api를 직접 의존하지 않는다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String originCtx = ContextKeys.contextKeyOf(origin);
                if (originCtx == null) {
                    return;
                }
                for (Dependency dep : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dep.getTargetClass();
                    String targetCtx = ContextKeys.contextKeyOf(target);
                    if (targetCtx == null || targetCtx.equals(originCtx)) {
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
```

`NO_CROSS_CONTEXT_INTERNALS` 아래에 규칙 추가:
```java
    public static final ArchRule CORE_BLIND_TO_FOREIGN_API =
            classes().that(IS_CORE)
                    .should(notDependOnOtherContextApi())
                    .as("core blind to foreign api")
                    .because("컨텍스트의 domain/application은 다른 컨텍스트의 .api조차 직접 의존하지 않는다 (자기 포트를 정의하고, 교차 의존은 어댑터에서만)")
                    .allowEmptyShould(true);
```

`getRules()` 를 두 규칙 반환으로 교체:
```java
    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "no cross-context internals",
                new ArchRuleSpec(NO_CROSS_CONTEXT_INTERNALS, Priority.HIGH),
                "core blind to foreign api",
                new ArchRuleSpec(CORE_BLIND_TO_FOREIGN_API, Priority.HIGH));
    }
```

- [ ] **Step 5: 테스트 통과 확인** — Run: `./gradlew :context-isolation-rules:test` / Expected: PASS (전체). RuleConventionTest가 2개 규칙 모두 검증.

- [ ] **Step 6: 커밋**

```bash
git add context-isolation-rules/src/main context-isolation-rules/src/test
git commit -m "feat(context-isolation-rules): R2 코어는 다른 컨텍스트 .api도 직접 의존 금지 + self-test

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

### Task 4: 통합 검증 — 전체 빌드 + ServiceLoader 발견

**Files:**
- Test: `context-isolation-rules/src/test/java/com/ryuqqq/archrules/context/ServiceLoaderDiscoveryTest.java`

**Interfaces:**
- Consumes: 신규 모듈 전체.

- [ ] **Step 1: 루트 전체 빌드** — Run: `./gradlew build` / Expected: 전 모듈 PASS(기존 domain-rules/hexagonal-rules 회귀 없음).

- [ ] **Step 2: ServiceLoader 발견 self-test 작성** — `ServiceLoaderDiscoveryTest.java`:

```java
package com.ryuqqq.archrules.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.api.ArchRulesService;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

class ServiceLoaderDiscoveryTest {

    @Test
    void isDiscoverableViaServiceLoader() {
        boolean found = StreamSupport.stream(
                        ServiceLoader.load(ArchRulesService.class).spliterator(), false)
                .anyMatch(s -> s instanceof ContextIsolationRules);
        assertTrue(found, "ContextIsolationRules 가 ServiceLoader로 발견되어야 한다");
    }
}
```

- [ ] **Step 3: 테스트 통과 확인** — Run: `./gradlew :context-isolation-rules:test --tests '*ServiceLoaderDiscoveryTest'` / Expected: PASS.

- [ ] **Step 4: 커밋**

```bash
git add context-isolation-rules/src/test/java/com/ryuqqq/archrules/context/ServiceLoaderDiscoveryTest.java
git commit -m "test(context-isolation-rules): ServiceLoader 발견 검증

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## 후속 plan (이 plan 범위 밖 — §4 나머지 규칙 그룹)

이 plan은 C-3(컨텍스트 격리)만 다룬다. 나머지는 각각 별도 plan으로:
- **C-2 확장**(`hexagonal-rules`): `port/out` 하위 패키지 화이트리스트({command,query,client,event}), `*Service`→`*UseCase` 구현, `*Manager` 단일 포트.
- **C-4 security-boundary-rules**: authhub 의존 금지(gateway 예외), public 어댑터 DTO 도메인 타입 미노출.
- **C-5 messaging-rules**: 이벤트 payload 도메인 타입 금지, `EventEnvelope` 사용, 멱등 consumer.
- **C-6 persistence-rules**: 영속 어댑터 자기 스키마 한정, JPA 엔티티 누출 금지.
- **C-7**: shared-kernel 역의존 0.
- **SSOT 통합**: spring-platform-commons archrules를 본 레포로 일원화.

---

## Self-Review

- **Spec coverage:** §4 C-3의 두 규칙(NO_CROSS_CONTEXT_INTERNALS, CORE_BLIND_TO_FOREIGN_API)과 "다리 어댑터만 교차 .api 허용"을 구현. C-1/C-2/C-4~C-7은 후속 plan으로 명시(범위 분리).
- **Placeholder scan:** 모든 스텝에 실제 코드/명령/기대출력 포함. TBD/TODO 없음.
- **Type consistency:** `ContextKeys.contextKeyOf/layerOf` 시그니처가 Task 1 정의와 Task 2/3 사용처 일치. 규칙 키 문자열("no cross-context internals", "core blind to foreign api")이 `.as()`·`getRules()` 키·테스트 `get(...)` 인자에서 일치. 픽스처 패키지 경로가 import와 일치.
