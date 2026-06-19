# 도메인 VO 규칙 추출 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** connectly `VOArchTest`의 VO 규칙을 archrules-platform SPI로 추출한다 — 보편 3규칙은 `domain-rules`, connectly 특화 3규칙은 신규 `connectly-domain-rules` 모듈.

**Architecture:** connectly 인라인 JUnit 규칙을 SPI(`ArchRulesService.getRules()` → `Map<String,ArchRuleSpec>`)로 변환한다. 상대 매처 `..vo..`를 유지(root 무관)하고, 커스텀 `ArchCondition`을 규칙 클래스에 `private static`으로 이식하며, 각 규칙에 `.as()`+`.because()`+priority를 부여한다. self-test는 `..vo..` 패키지의 compliant/violation 픽스처를 `Runner.check(rule, Fixture.class)`로 평가한다.

**Tech Stack:** Java 21, ArchUnit 1.2.1, JUnit 5.11.4.

## Global Constraints

- **외부 빌드 의존 0**: ArchUnit·JUnit 외 서드파티 추가 금지(테스트 포함).
- **Java 21** toolchain.
- **상대 매처만**: `..vo..` 사용, root 패키지 하드코딩 금지(이식성).
- 모든 노출 규칙은 **`.as(name)` + `.because(reason)` + priority + `allowEmptyShould(true)`**.
- 규칙 1·2·8의 `that()` 필터(Enum/Interface/Abstract/`Fixture`/`Mother`/`Test`/Anonymous/Member 제외)는 원본대로 유지 — **픽스처 클래스 이름에 `Fixture`/`Mother`/`Test`를 넣지 말 것**(그러면 규칙 대상에서 빠져 self-test가 무의미해짐).
- 어노테이션 금지(lombok/jpa/spring) 규칙은 추출하지 않음(spec 결정 4).

---

## File Structure

| 파일 | 책임 | 변경 |
|------|------|------|
| `domain-rules/.../domain/VoRules.java` | VO 보편 3규칙 + ArchCondition 헬퍼 | Create |
| `domain-rules/.../domain/DomainRules.java` | getRules()에 VO 3규칙 합류 | Modify |
| `domain-rules/src/test/.../fixture/compliant/domain/vo/CompliantMoney.java` | 적합 VO | Create |
| `domain-rules/src/test/.../fixture/violation/domain/vo/NotARecordVo.java` | record 위반 | Create |
| `domain-rules/src/test/.../fixture/violation/domain/vo/NoFactoryVo.java` | of 위반 | Create |
| `domain-rules/src/test/.../fixture/violation/domain/vo/CreateMethodVo.java` | create 위반 | Create |
| `domain-rules/src/test/.../domain/VoRulesTest.java` | VO 보편 규칙 self-test | Create |
| `settings.gradle` | connectly-domain-rules 등록 | Modify |
| `connectly-domain-rules/build.gradle` | 모듈 빌드 | Create |
| `connectly-domain-rules/.../connectly/ConnectlyDomainRules.java` | 특화 3규칙 | Create |
| `connectly-domain-rules/src/main/resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService` | SPI 등록 | Create |
| `connectly-domain-rules/src/test/.../fixture/**` | compliant/violation 픽스처 | Create |
| `connectly-domain-rules/.../connectly/ConnectlyDomainRulesTest.java` | 특화 규칙 self-test | Create |
| `connectly-domain-rules/.../connectly/RuleConventionTest.java` | 컨벤션 강제 | Create |

패키지 베이스: domain-rules는 `com.ryuqqq.archrules.domain`, 신규 모듈은 `com.ryuqqq.archrules.connectly`.

---

### Task 1: domain-rules — VO 보편 3규칙

**Files:** 위 표의 domain-rules 항목.

**Interfaces:**
- Consumes: `ArchRulesService`/`ArchRuleSpec`(archrules-api), `Runner.check`(archrules-runtime, test).
- Produces: `DomainRules.getRules()`에 키 `"domain VO is record"`, `"domain VO has static factory of"`, `"domain VO has no create method"` 추가. `VoRules.VO_IS_RECORD`/`VO_HAS_OF`/`VO_NO_CREATE`(`ArchRule` 상수, package-private).

- [ ] **Step 1: 픽스처 4개 생성**

`domain-rules/src/test/java/com/ryuqqq/archrules/domain/fixture/compliant/domain/vo/CompliantMoney.java`:
```java
package com.ryuqqq.archrules.domain.fixture.compliant.domain.vo;

/** record + of() + create 없음 → VO 보편 3규칙 모두 통과. */
public record CompliantMoney(long amount) {
    public static CompliantMoney of(long amount) {
        return new CompliantMoney(amount);
    }
}
```

`domain-rules/src/test/java/com/ryuqqq/archrules/domain/fixture/violation/domain/vo/NotARecordVo.java`:
```java
package com.ryuqqq.archrules.domain.fixture.violation.domain.vo;

/** record가 아님(class) → "domain VO is record"만 위반. of()는 있어 of 규칙은 통과. */
public final class NotARecordVo {
    private final long value;
    private NotARecordVo(long value) { this.value = value; }
    public static NotARecordVo of(long value) { return new NotARecordVo(value); }
    public long value() { return value; }
}
```

`domain-rules/src/test/java/com/ryuqqq/archrules/domain/fixture/violation/domain/vo/NoFactoryVo.java`:
```java
package com.ryuqqq.archrules.domain.fixture.violation.domain.vo;

/** record지만 of() 없음 → "domain VO has static factory of"만 위반. */
public record NoFactoryVo(long amount) {}
```

`domain-rules/src/test/java/com/ryuqqq/archrules/domain/fixture/violation/domain/vo/CreateMethodVo.java`:
```java
package com.ryuqqq.archrules.domain.fixture.violation.domain.vo;

/** record + of() 이지만 create* 메서드 보유 → "domain VO has no create method"만 위반. */
public record CreateMethodVo(long amount) {
    public static CreateMethodVo of(long amount) { return new CreateMethodVo(amount); }
    public static CreateMethodVo createDefault() { return new CreateMethodVo(0L); }
}
```

- [ ] **Step 2: 실패 테스트 작성**

`domain-rules/src/test/java/com/ryuqqq/archrules/domain/VoRulesTest.java`:
```java
package com.ryuqqq.archrules.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.vo.CompliantMoney;
import com.ryuqqq.archrules.domain.fixture.violation.domain.vo.CreateMethodVo;
import com.ryuqqq.archrules.domain.fixture.violation.domain.vo.NoFactoryVo;
import com.ryuqqq.archrules.domain.fixture.violation.domain.vo.NotARecordVo;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class VoRulesTest {

    private final DomainRules svc = new DomainRules();
    private ArchRule rule(String key) { return svc.getRules().get(key).rule(); }

    @Test
    void compliantVoPassesAllThree() {
        assertFalse(Runner.check(rule("domain VO is record"), CompliantMoney.class).hasViolation());
        assertFalse(Runner.check(rule("domain VO has static factory of"), CompliantMoney.class).hasViolation());
        assertFalse(Runner.check(rule("domain VO has no create method"), CompliantMoney.class).hasViolation());
    }

    @Test
    void nonRecordViolatesRecordRule() {
        assertTrue(Runner.check(rule("domain VO is record"), NotARecordVo.class).hasViolation());
    }

    @Test
    void missingOfViolatesFactoryRule() {
        assertTrue(Runner.check(rule("domain VO has static factory of"), NoFactoryVo.class).hasViolation());
    }

    @Test
    void createMethodViolatesNoCreateRule() {
        assertTrue(Runner.check(rule("domain VO has no create method"), CreateMethodVo.class).hasViolation());
    }
}
```

- [ ] **Step 3: 테스트 실행 → 실패 확인**

Run: `./gradlew :domain-rules:test --tests '*VoRulesTest' -q`
Expected: 컴파일 에러 — `getRules()`에 `"domain VO is record"` 등 키 없음(NullPointerException 또는 키 부재).

- [ ] **Step 4: VoRules 구현**

`domain-rules/src/main/java/com/ryuqqq/archrules/domain/VoRules.java`:
```java
package com.ryuqqq.archrules.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** VO 작성 컨벤션(보편) — 상대 매처 ..vo.. 로 root 무관. */
final class VoRules {

    private VoRules() {}

    private static final String VO = "..vo..";

    static final ArchRule VO_IS_RECORD =
            classes().that().resideInAPackage(VO)
                    .and().areNotEnums()
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(beRecords())
                    .as("domain VO is record")
                    .because("Value Object는 Java record로 구현한다(Enum/Interface/Abstract 제외)")
                    .allowEmptyShould(true);

    static final ArchRule VO_HAS_OF =
            classes().that().resideInAPackage(VO)
                    .and().areNotEnums()
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(haveStaticMethodWithName("of"))
                    .as("domain VO has static factory of")
                    .because("Value Object는 of() 정적 팩토리로 생성한다")
                    .allowEmptyShould(true);

    static final ArchRule VO_NO_CREATE =
            classes().that().resideInAPackage(VO)
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(notHaveMethodsWithNameStartingWith("create"))
                    .as("domain VO has no create method")
                    .because("Value Object는 create*() 대신 of()/forNew()를 쓴다")
                    .allowEmptyShould(true);

    /** Java record 여부(java.lang.Record 상속) 검증. */
    private static ArchCondition<JavaClass> beRecords() {
        return new ArchCondition<JavaClass>("be records") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean isRecord = javaClass.getAllRawSuperclasses().stream()
                        .anyMatch(s -> s.getName().equals("java.lang.Record"));
                if (!isRecord) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            String.format("Class %s is not a record", javaClass.getName())));
                }
            }
        };
    }

    /** public static 메서드 존재 검증. */
    private static ArchCondition<JavaClass> haveStaticMethodWithName(String methodName) {
        return new ArchCondition<JavaClass>("have public static method named " + methodName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean has = javaClass.getAllMethods().stream()
                        .anyMatch(m -> m.getName().equals(methodName)
                                && m.getModifiers().contains(JavaModifier.STATIC)
                                && m.getModifiers().contains(JavaModifier.PUBLIC));
                if (!has) {
                    events.add(SimpleConditionEvent.violated(javaClass, String.format(
                            "Class %s has no public static method '%s'", javaClass.getName(), methodName)));
                }
            }
        };
    }

    /** 특정 접두 메서드 부재 검증. */
    private static ArchCondition<JavaClass> notHaveMethodsWithNameStartingWith(String prefix) {
        return new ArchCondition<JavaClass>("not have methods named starting with " + prefix) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getAllMethods().stream()
                        .filter(m -> m.getName().startsWith(prefix))
                        .forEach(m -> events.add(SimpleConditionEvent.violated(javaClass, String.format(
                                "Class %s has prohibited method '%s' starting with '%s'",
                                javaClass.getName(), m.getName(), prefix))));
            }
        };
    }
}
```

- [ ] **Step 5: DomainRules.getRules()에 VO 규칙 합류**

`DomainRules.java`의 `getRules()`를 아래로 교체(기존 2규칙 유지 + VO 3규칙 추가, `Map.of`는 10쌍까지 가능):
```java
    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "domain reads no clock", new ArchRuleSpec(NO_TIME_IN_DOMAIN, Priority.HIGH),
                "domain has no setters", new ArchRuleSpec(NO_SETTERS_IN_DOMAIN, Priority.MEDIUM),
                "domain VO is record", new ArchRuleSpec(VoRules.VO_IS_RECORD, Priority.HIGH),
                "domain VO has static factory of", new ArchRuleSpec(VoRules.VO_HAS_OF, Priority.HIGH),
                "domain VO has no create method", new ArchRuleSpec(VoRules.VO_NO_CREATE, Priority.MEDIUM));
    }
```

- [ ] **Step 6: 테스트 실행 → 통과 확인**

Run: `./gradlew :domain-rules:test -q`
Expected: PASS — `VoRulesTest`(4) + 기존 `DomainRulesTest`·`RuleConventionTest`(VO 규칙도 .as/priority/because 보유하므로 `RuleConventionTest` 통과).

- [ ] **Step 7: 커밋**

```bash
git add domain-rules/src/main/java/com/ryuqqq/archrules/domain/VoRules.java \
        domain-rules/src/main/java/com/ryuqqq/archrules/domain/DomainRules.java \
        domain-rules/src/test/java/com/ryuqqq/archrules/domain/VoRulesTest.java \
        domain-rules/src/test/java/com/ryuqqq/archrules/domain/fixture/
git commit -m "feat(domain-rules): VO 보편 규칙 추출(record/of/create금지)"
```

---

### Task 2: connectly-domain-rules — 특화 3규칙(신규 모듈)

**Files:** 위 표의 connectly-domain-rules 항목 + `settings.gradle`.

**Interfaces:**
- Consumes: `ArchRulesService`/`ArchRuleSpec`(archrules-api), `Runner.check`(archrules-runtime, test).
- Produces: `ConnectlyDomainRules implements ArchRulesService`, 키 `"connectly id VO has forNew"`/`"connectly long id VO has isNew"`/`"connectly enum VO has displayName"`.

- [ ] **Step 1: 모듈 등록 — settings.gradle + build.gradle**

`settings.gradle`에 한 줄 추가:
```groovy
include 'connectly-domain-rules'
```

`connectly-domain-rules/build.gradle` 생성(domain-rules와 동일 패턴):
```groovy
dependencies {
    api project(':archrules-api')
    testImplementation project(':archrules-runtime')
    testImplementation platform(libs.junit.bom)
    testImplementation libs.junit.jupiter
    testImplementation libs.archunit
}
```

- [ ] **Step 2: SPI 등록 + 픽스처 5개**

`connectly-domain-rules/src/main/resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService`:
```
com.ryuqqq.archrules.connectly.ConnectlyDomainRules
```

compliant `connectly-domain-rules/src/test/java/com/ryuqqq/archrules/connectly/fixture/compliant/domain/vo/UserId.java`:
```java
package com.ryuqqq.archrules.connectly.fixture.compliant.domain.vo;

/** Long ID VO — forNew()/isNew() 보유 → forNew·isNew 규칙 통과. */
public record UserId(Long value) {
    public static UserId of(Long value) { return new UserId(value); }
    public static UserId forNew() { return new UserId(null); }
    public boolean isNew() { return value == null; }
}
```

compliant `.../fixture/compliant/domain/vo/Color.java`:
```java
package com.ryuqqq.archrules.connectly.fixture.compliant.domain.vo;

/** Enum VO — displayName() 보유 → displayName 규칙 통과. */
public enum Color {
    RED;
    public String displayName() { return name(); }
}
```

violation `.../fixture/violation/domain/vo/NoForNewId.java`:
```java
package com.ryuqqq.archrules.connectly.fixture.violation.domain.vo;

/** Id로 끝나지만 forNew() 없음 → "connectly id VO has forNew"만 위반(isNew는 보유). */
public record NoForNewId(Long value) {
    public boolean isNew() { return value == null; }
}
```

violation `.../fixture/violation/domain/vo/NoIsNewId.java`:
```java
package com.ryuqqq.archrules.connectly.fixture.violation.domain.vo;

/** Long 필드 보유 + isNew() 없음 → "connectly long id VO has isNew"만 위반(forNew는 보유). */
public record NoIsNewId(Long value) {
    public static NoIsNewId forNew() { return new NoIsNewId(null); }
}
```

violation `.../fixture/violation/domain/vo/PlainStatus.java`:
```java
package com.ryuqqq.archrules.connectly.fixture.violation.domain.vo;

/** Enum이지만 displayName() 없음 → "connectly enum VO has displayName"만 위반. */
public enum PlainStatus {
    ON
}
```

- [ ] **Step 3: 실패 테스트 작성 (self-test + 컨벤션)**

`connectly-domain-rules/src/test/java/com/ryuqqq/archrules/connectly/ConnectlyDomainRulesTest.java`:
```java
package com.ryuqqq.archrules.connectly;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.connectly.fixture.compliant.domain.vo.Color;
import com.ryuqqq.archrules.connectly.fixture.compliant.domain.vo.UserId;
import com.ryuqqq.archrules.connectly.fixture.violation.domain.vo.NoForNewId;
import com.ryuqqq.archrules.connectly.fixture.violation.domain.vo.NoIsNewId;
import com.ryuqqq.archrules.connectly.fixture.violation.domain.vo.PlainStatus;
import com.ryuqqq.archrules.runtime.Runner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class ConnectlyDomainRulesTest {

    private final ConnectlyDomainRules svc = new ConnectlyDomainRules();
    private ArchRule rule(String key) { return svc.getRules().get(key).rule(); }

    @Test
    void compliantIdAndEnumPass() {
        assertFalse(Runner.check(rule("connectly id VO has forNew"), UserId.class).hasViolation());
        assertFalse(Runner.check(rule("connectly long id VO has isNew"), UserId.class).hasViolation());
        assertFalse(Runner.check(rule("connectly enum VO has displayName"), Color.class).hasViolation());
    }

    @Test
    void missingForNewViolates() {
        assertTrue(Runner.check(rule("connectly id VO has forNew"), NoForNewId.class).hasViolation());
    }

    @Test
    void missingIsNewViolates() {
        assertTrue(Runner.check(rule("connectly long id VO has isNew"), NoIsNewId.class).hasViolation());
    }

    @Test
    void missingDisplayNameViolates() {
        assertTrue(Runner.check(rule("connectly enum VO has displayName"), PlainStatus.class).hasViolation());
    }
}
```

`connectly-domain-rules/src/test/java/com/ryuqqq/archrules/connectly/RuleConventionTest.java`:
```java
package com.ryuqqq.archrules.connectly;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuleConventionTest {

    private final Map<String, ArchRuleSpec> rules = new ConnectlyDomainRules().getRules();

    @Test
    void everyRuleHasPriorityAndBecause() {
        assertFalse(rules.isEmpty());
        rules.forEach((name, spec) -> {
            assertNotNull(spec.priority(), name + " priority non-null");
            String desc = spec.rule().getDescription();
            assertTrue(desc != null && desc.contains("because"), name + " has because: " + desc);
        });
    }
}
```

- [ ] **Step 4: 테스트 실행 → 실패 확인**

Run: `./gradlew :connectly-domain-rules:test -q`
Expected: 컴파일 에러 — `ConnectlyDomainRules` 클래스 없음.

- [ ] **Step 5: ConnectlyDomainRules 구현**

`connectly-domain-rules/src/main/java/com/ryuqqq/archrules/connectly/ConnectlyDomainRules.java`:
```java
package com.ryuqqq.archrules.connectly;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Map;

/** connectly platform-common-domain VO 컨벤션(특화) — 상대 매처 ..vo.. 로 root 무관. */
public final class ConnectlyDomainRules implements ArchRulesService {

    private static final String VO = "..vo..";

    static final ArchRule ID_HAS_FORNEW =
            classes().that().resideInAPackage(VO)
                    .and().haveSimpleNameEndingWith("Id")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(haveStaticMethodWithName("forNew"))
                    .as("connectly id VO has forNew")
                    .because("ID VO는 forNew() 팩토리를 가진다(Long=null, UUID=UUIDv7)")
                    .allowEmptyShould(true);

    static final ArchRule LONG_ID_HAS_ISNEW =
            classes().that().resideInAPackage(VO)
                    .and().haveSimpleNameEndingWith("Id")
                    .and().areNotInterfaces()
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .and().areNotAnonymousClasses()
                    .and().areNotMemberClasses()
                    .should(haveLongFieldAndIsNewMethod())
                    .as("connectly long id VO has isNew")
                    .because("Long ID VO는 isNew()로 신규 여부를 판별한다(UUID 면제)")
                    .allowEmptyShould(true);

    static final ArchRule ENUM_HAS_DISPLAYNAME =
            classes().that().resideInAPackage(VO)
                    .and().areEnums()
                    .and().haveSimpleNameNotContaining("Fixture")
                    .and().haveSimpleNameNotContaining("Mother")
                    .and().haveSimpleNameNotContaining("Test")
                    .should(haveMethodWithName("displayName"))
                    .as("connectly enum VO has displayName")
                    .because("Enum VO는 displayName()으로 표시명을 제공한다")
                    .allowEmptyShould(true);

    @Override
    public Map<String, ArchRuleSpec> getRules() {
        return Map.of(
                "connectly id VO has forNew", new ArchRuleSpec(ID_HAS_FORNEW, Priority.HIGH),
                "connectly long id VO has isNew", new ArchRuleSpec(LONG_ID_HAS_ISNEW, Priority.MEDIUM),
                "connectly enum VO has displayName", new ArchRuleSpec(ENUM_HAS_DISPLAYNAME, Priority.MEDIUM));
    }

    /** public static 메서드 존재 검증. */
    private static ArchCondition<JavaClass> haveStaticMethodWithName(String methodName) {
        return new ArchCondition<JavaClass>("have public static method named " + methodName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean has = javaClass.getAllMethods().stream()
                        .anyMatch(m -> m.getName().equals(methodName)
                                && m.getModifiers().contains(JavaModifier.STATIC)
                                && m.getModifiers().contains(JavaModifier.PUBLIC));
                if (!has) {
                    events.add(SimpleConditionEvent.violated(javaClass, String.format(
                            "Class %s has no public static method '%s'", javaClass.getName(), methodName)));
                }
            }
        };
    }

    /** 임의 이름 메서드 존재 검증(static 무관). */
    private static ArchCondition<JavaClass> haveMethodWithName(String methodName) {
        return new ArchCondition<JavaClass>("have method named " + methodName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean has = javaClass.getAllMethods().stream()
                        .anyMatch(m -> m.getName().equals(methodName));
                if (!has) {
                    events.add(SimpleConditionEvent.violated(javaClass, String.format(
                            "Class %s has no method '%s'", javaClass.getName(), methodName)));
                }
            }
        };
    }

    /** Long 필드 보유 ID VO는 isNew() 필수. String(UUID) 전용이면 면제. */
    private static ArchCondition<JavaClass> haveLongFieldAndIsNewMethod() {
        return new ArchCondition<JavaClass>("have Long field and isNew() method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasLongField = javaClass.getAllFields().stream()
                        .anyMatch(f -> f.getRawType().getName().equals("java.lang.Long")
                                || f.getRawType().getName().equals("long"));
                boolean hasStringFieldOnly = javaClass.getAllFields().stream()
                        .anyMatch(f -> f.getRawType().getName().equals("java.lang.String"))
                        && !hasLongField;
                if (hasStringFieldOnly) {
                    return; // UUID(String) ID는 면제
                }
                if (hasLongField) {
                    boolean hasIsNew = javaClass.getAllMethods().stream()
                            .anyMatch(m -> m.getName().equals("isNew"));
                    if (!hasIsNew) {
                        events.add(SimpleConditionEvent.violated(javaClass, String.format(
                                "Long ID VO %s must have isNew() method", javaClass.getName())));
                    }
                }
            }
        };
    }
}
```

- [ ] **Step 6: 테스트 실행 → 통과 확인**

Run: `./gradlew :connectly-domain-rules:test -q`
Expected: PASS — `ConnectlyDomainRulesTest`(4) + `RuleConventionTest`(1).

- [ ] **Step 7: 전체 빌드 회귀 확인**

Run: `./gradlew build -q`
Expected: BUILD SUCCESSFUL — 신규 모듈 포함 전 모듈 self-test 통과. (domain-rules·connectly-domain-rules 둘 다 ServiceLoader에 등록되지만 self-test는 `Runner.check(특정 rule, fixture)`라 상호 간섭 없음.)

- [ ] **Step 8: 커밋**

```bash
git add settings.gradle connectly-domain-rules/
git commit -m "feat(connectly-domain-rules): 신규 모듈 + VO 특화 규칙(forNew/isNew/displayName)"
```

---

## Self-Review

**1. Spec coverage:** spec §3 6규칙 — record/of/create금지(Task 1), forNew/isNew/displayName(Task 2). §4 모듈 구조(VoRules+DomainRules, 신규 모듈+SPI 등록) 모두 task에 대응. §2 결정 4(어노테이션 금지 제외) — 해당 규칙 없음으로 반영. 갭 없음.

**2. Placeholder scan:** 모든 코드 step에 완전한 코드. TBD/"적절히" 없음. ArchCondition 전체 본문 포함.

**3. Type consistency:** 규칙 키 문자열이 VoRules/DomainRules/VoRulesTest(Task1), ConnectlyDomainRules/ConnectlyDomainRulesTest(Task2)에서 동일. `Runner.check(ArchRule, Class...)`·`getRules().get(key).rule()` 시그니처 기존 `DomainRulesTest`와 일치. 픽스처 패키지에 `.vo.` 세그먼트 포함 → `..vo..` 매칭 보장. 픽스처 이름에 Fixture/Mother/Test 미포함(필터 회피 방지).

**범위 밖:** aggregate/exception/criteria/package/purity 카테고리, connectly-services 인라인 룰 제거·소비(#3).
