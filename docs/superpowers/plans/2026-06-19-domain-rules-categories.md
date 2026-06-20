# 도메인 규칙 카테고리 추출 + 모듈 병합 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** connectly 도메인 architecture 규칙(aggregate/exception/criteria/package)을 archrules-platform `domain-rules`로 추출하고, `connectly-domain-rules` 모듈을 `domain-rules`로 병합하며, purity 규칙은 hexagonal-rules `framework-free`를 확장해 흡수한다.

**Architecture:** 각 카테고리는 자기 규칙 맵을 소유하는 클래스(`AggregateRules.rules()` 등)로 만들고, `DomainRules`(단일 `ArchRulesService`)가 모든 `rules()`를 병합만 한다. 공용 ArchCondition은 package-private `ArchConditions` 유틸로 추출(Task 0에서 확정, 카테고리는 읽기만 → 병렬 안전). 각 카테고리는 독립 파일집합(`XxxRules` + 픽스처 + `XxxRulesTest`)이라 병렬 작성 안전하고, 유일한 공유 파일 `DomainRules.java`는 통합 Task에서 순차 병합한다.

**Tech Stack:** Java 21, ArchUnit 1.2.1, JUnit 5.11.4.

## 카테고리 task의 작업 방식 (Task 1~4)

카테고리 task는 **connectly 원본 파일을 참조해 변환**한다. 원본은 실재하며 각 규칙의 완전한 코드(빌트인 호출·커스텀 ArchCondition 본문)를 담고 있다. 각 task는 (a) 원본 경로, (b) 남길/버릴 규칙 목록과 priority, (c) 변환 규칙, (d) 본보기(`VoRules.java`)를 제공한다. 본보기와 동일한 패턴으로 변환하라.

**변환 규칙 (모든 카테고리 공통):**
1. 원본 `@Test` 메서드 1개 → `XxxRules`의 `static final ArchRule` 상수 1개 + `rules()` 맵 항목 1개.
2. 패키지 매처는 **상대 매처만**(`..aggregate..` 등). 원본의 `BASE_PACKAGE`/`com.connectly.crawling` 하드코딩은 제거하고 상대 매처로 전환. (import는 runtime이 담당하므로 `importPackages`·`@BeforeAll` 불필요.)
3. 각 규칙에 `.as(name)` + `.because(reason)` + `.allowEmptyShould(true)` 부여. priority는 `rules()`의 `ArchRuleSpec`에 싣는다.
4. 원본의 `that()` 필터(Enum/Interface/Abstract/Fixture/Mother/Test/Anonymous/Member 제외 등)는 원본대로 유지. 픽스처 클래스 이름에 `Fixture`/`Mother`/`Test` 금지.
5. 공용 ArchCondition(`beRecords`/`haveStaticMethodWithName`/`haveMethodWithName`/`notHaveMethodsWithNameStartingWith`)은 Task 0의 `ArchConditions`를 호출. 카테고리 전용 ArchCondition은 그 카테고리 파일에 `private static`으로 둔다(공용 추출은 이번 범위 밖, 중복 허용).
6. self-test: 각 violation 픽스처가 **의도한 규칙 1개만** 위반하도록 설계. `XxxRulesTest`가 `XxxRules.rules().get(key).rule()`을 `Runner.check(rule, Fixture.class)`로 평가.

## Global Constraints

- **외부 빌드 의존 0**: ArchUnit·JUnit 외 서드파티 추가 금지(테스트 포함).
- **Java 21** toolchain.
- **상대 매처만**: root 패키지 하드코딩 금지(이식성).
- 모든 노출 규칙은 **`.as(name)` + `.because(reason)` + priority + `allowEmptyShould(true)`**.
- **어노테이션 금지(lombok/jpa/spring) 규칙은 추출하지 않음** (lombok=no-op, jpa/spring=framework-free 중복).
- **soft/문서용 규칙은 추출하지 않음** (ArchRule이 아니거나 항상 통과하는 규칙).
- 보편/특화 구분은 코드 레벨(클래스/규칙 이름)에서만 유지, 모듈은 `domain-rules` 하나.
- `Map.of`는 10쌍 한계 — 11개 이상이면 `Map.ofEntries` 또는 `HashMap.putAll` 사용.

---

## File Structure

| 파일 | 책임 | Task |
|------|------|------|
| `domain-rules/.../domain/ArchConditions.java` | 공용 ArchCondition 유틸(package-private) | 0 |
| `domain-rules/.../domain/VoRules.java` | VO 보편 규칙 + `rules()` (ArchConditions 사용으로 정렬) | 0 |
| `domain-rules/.../domain/ConnectlyVoRules.java` | VO 특화 규칙(이동) + `rules()` | 0 |
| `domain-rules/.../domain/DomainRules.java` | 모든 카테고리 `rules()` 병합 | 0,5 |
| `domain-rules/src/test/.../domain/ConnectlyVoRulesTest.java` | 특화 VO self-test(이동) | 0 |
| `domain-rules/src/test/.../fixture/**/vo/*` | VO 특화 픽스처(이동) | 0 |
| `connectly-domain-rules/` (전체) | 삭제 | 0 |
| `settings.gradle` | `connectly-domain-rules` include 제거 | 0 |
| `domain-rules/.../domain/AggregateRules.java` + 픽스처 + Test | aggregate 규칙 | 1 |
| `domain-rules/.../domain/ExceptionRules.java` + 픽스처 + Test | exception 규칙 | 2 |
| `domain-rules/.../domain/CriteriaRules.java` + 픽스처 + Test | criteria 규칙 | 3 |
| `domain-rules/.../domain/PackageRules.java` + 픽스처 + Test | package 규칙 | 4 |
| `hexagonal-rules/.../hexagonal/HexagonalRules.java` + 픽스처 + Test | framework-free 확장 | 6 |

패키지 베이스: `com.ryuqqq.archrules.domain`. 픽스처 베이스: `com.ryuqqq.archrules.domain.fixture.{compliant,violation}.domain.<category>`.

---

## Task 0: 모듈 병합 + rules() 병합 구조 + 공용 ArchConditions (baseline)

**Files:** 위 표 Task 0 항목.

**Interfaces:**
- Produces: `ArchConditions.beRecords()` / `haveStaticMethodWithName(String)` / `haveMethodWithName(String)` / `notHaveMethodsWithNameStartingWith(String)` (각 `ArchCondition<JavaClass>` 반환, package-private). `VoRules.rules()` / `ConnectlyVoRules.rules()` (`Map<String,ArchRuleSpec>`). `DomainRules.getRules()`가 모든 카테고리 `rules()`를 병합.

- [ ] **Step 1: 공용 ArchConditions 생성**

`domain-rules/src/main/java/com/ryuqqq/archrules/domain/ArchConditions.java`:
```java
package com.ryuqqq.archrules.domain;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** 카테고리 규칙이 공유하는 ArchCondition 모음(package-private). */
final class ArchConditions {

    private ArchConditions() {}

    /** Java record 여부(java.lang.Record 상속) 검증. */
    static ArchCondition<JavaClass> beRecords() {
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
    static ArchCondition<JavaClass> haveStaticMethodWithName(String methodName) {
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
    static ArchCondition<JavaClass> haveMethodWithName(String methodName) {
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

    /** 특정 접두 메서드 부재 검증. */
    static ArchCondition<JavaClass> notHaveMethodsWithNameStartingWith(String prefix) {
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

- [ ] **Step 2: VoRules를 ArchConditions 사용 + rules() 제공으로 정렬**

`VoRules.java`에서 자체 `private static ArchCondition` 3개(`beRecords`/`haveStaticMethodWithName`/`notHaveMethodsWithNameStartingWith`)를 **삭제**하고, 규칙 본문의 `.should(beRecords())` → `.should(ArchConditions.beRecords())`, `.should(haveStaticMethodWithName("of"))` → `.should(ArchConditions.haveStaticMethodWithName("of"))`, `.should(notHaveMethodsWithNameStartingWith("create"))` → `.should(ArchConditions.notHaveMethodsWithNameStartingWith("create"))` 로 교체. 그리고 클래스 끝에 `rules()` 추가:
```java
    static java.util.Map<String, com.ryuqqq.archrules.api.ArchRuleSpec> rules() {
        return java.util.Map.of(
                "domain VO is record",
                        new com.ryuqqq.archrules.api.ArchRuleSpec(VO_IS_RECORD, com.tngtech.archunit.lang.Priority.HIGH),
                "domain VO has static factory of",
                        new com.ryuqqq.archrules.api.ArchRuleSpec(VO_HAS_OF, com.tngtech.archunit.lang.Priority.HIGH),
                "domain VO has no create method",
                        new com.ryuqqq.archrules.api.ArchRuleSpec(VO_NO_CREATE, com.tngtech.archunit.lang.Priority.MEDIUM));
    }
```
(import를 파일 상단에 정리해도 됨 — 기존 import 스타일을 따른다.)

- [ ] **Step 3: ConnectlyVoRules 생성(connectly-domain-rules에서 이동)**

`domain-rules/src/main/java/com/ryuqqq/archrules/domain/ConnectlyVoRules.java` 생성. 기존 `connectly-domain-rules/.../connectly/ConnectlyDomainRules.java`의 내용을 옮기되: 패키지를 `com.ryuqqq.archrules.domain`로, 클래스명을 `ConnectlyVoRules`로, `implements ArchRulesService` 제거, `getRules()`를 `static Map<String,ArchRuleSpec> rules()`로 변경(본문 동일), 공용 헬퍼(`haveStaticMethodWithName`/`haveMethodWithName`)는 삭제하고 `ArchConditions.*` 호출로 교체. 카테고리 전용 `haveLongFieldAndIsNewMethod()`는 이 파일에 `private static`으로 유지. 클래스는 package-private(`final class ConnectlyVoRules`).

- [ ] **Step 4: DomainRules를 병합 구조로 리팩토링**

`DomainRules.java`의 `getRules()`를 교체:
```java
    @Override
    public Map<String, ArchRuleSpec> getRules() {
        java.util.Map<String, ArchRuleSpec> all = new java.util.HashMap<>();
        all.put("domain reads no clock", new ArchRuleSpec(NO_TIME_IN_DOMAIN, Priority.HIGH));
        all.put("domain has no setters", new ArchRuleSpec(NO_SETTERS_IN_DOMAIN, Priority.MEDIUM));
        all.putAll(VoRules.rules());
        all.putAll(ConnectlyVoRules.rules());
        return Map.copyOf(all);
    }
```
(기존 `NO_TIME_IN_DOMAIN`·`NO_SETTERS_IN_DOMAIN` 상수는 유지.)

- [ ] **Step 5: 특화 VO 픽스처·테스트 이동**

`connectly-domain-rules/src/test/.../fixture/{compliant,violation}/domain/vo/*` 5개 픽스처를 `domain-rules/src/test/java/com/ryuqqq/archrules/domain/fixture/{compliant,violation}/domain/vo/`로 이동(패키지를 `com.ryuqqq.archrules.domain.fixture...`로 변경). 단 VO 보편 픽스처(`CompliantMoney` 등)와 이름 충돌 없는지 확인 — `UserId`/`Color`/`NoForNewId`/`NoIsNewId`/`PlainStatus`는 충돌 없음.

`connectly-domain-rules/.../connectly/ConnectlyDomainRulesTest.java`를 `domain-rules/src/test/java/com/ryuqqq/archrules/domain/ConnectlyVoRulesTest.java`로 이동: 패키지 변경, `private final ConnectlyDomainRules svc = ...` → `rule(key)`를 `ConnectlyVoRules.rules().get(key).rule()`로 변경, 픽스처 import 경로 변경. (connectly의 `RuleConventionTest`는 이동하지 않고 삭제 — domain-rules의 기존 `RuleConventionTest`가 `new DomainRules().getRules()` 전체를 검사하므로 병합된 특화 규칙도 자동 커버.)

- [ ] **Step 6: connectly-domain-rules 모듈 제거**

```bash
git rm -r connectly-domain-rules
```
`settings.gradle`에서 `include 'connectly-domain-rules'` 줄 삭제.

- [ ] **Step 7: 전체 빌드 — baseline 검증**

Run: `./gradlew build -q`
Expected: BUILD SUCCESSFUL. domain-rules의 `VoRulesTest`(VO 보편 4) + `ConnectlyVoRulesTest`(특화 4) + `DomainRulesTest`(기존 3) + `RuleConventionTest`(이제 도메인2+VO보편3+특화3=8규칙 컨벤션 검사) 통과. `connectly-domain-rules` 디렉토리 없음.

- [ ] **Step 8: 커밋**

```bash
git add -A
git commit -m "refactor(domain-rules): connectly-domain-rules 흡수 + rules() 병합 구조 + 공용 ArchConditions"
```

---

## Task 1: AggregateRules (병렬)

**원본:** `/Users/ryu-qqq/Documents/ryu-qqq/connectly-services/services/crawling/crawling-domain/src/test/java/com/connectly/crawling/domain/architecture/aggregate/AggregateArchTest.java`

**Files:**
- Create: `domain-rules/src/main/java/com/ryuqqq/archrules/domain/AggregateRules.java`
- Create: `domain-rules/src/test/java/com/ryuqqq/archrules/domain/AggregateRulesTest.java`
- Create: 픽스처 `domain-rules/src/test/java/com/ryuqqq/archrules/domain/fixture/{compliant,violation}/domain/aggregate/*`

**Interfaces:**
- Consumes: `ArchConditions.haveStaticMethodWithName(String)` (Task 0), `Runner.check` (test).
- Produces: `AggregateRules.rules()` → `Map<String,ArchRuleSpec>`. 키 목록은 아래 표의 `.as` 이름.

**상대 매처:** aggregate 패키지 = `..aggregate..`.

**남길 규칙 (큐레이션):**

| 원본# | 의도 | 빌트인/커스텀 | priority | `.as` 이름 |
|---|---|---|---|---|
| 4 | setter 메서드 금지 | 커스텀 `notHaveSetterMethods()`(원본서 복사, private) | HIGH | `aggregate has no setters` |
| 5 | 생성자 private | 빌트인 `bePrivate()` | HIGH | `aggregate constructor is private` |
| 6 | `forNew()` 정적팩토리 | `ArchConditions.haveStaticMethodWithName("forNew")` | HIGH | `aggregate has forNew` |
| 7 | `of()` 정적팩토리 | `ArchConditions.haveStaticMethodWithName("of")` | HIGH | `aggregate has of` |
| 8 | `reconstitute()` 정적팩토리 | `ArchConditions.haveStaticMethodWithName("reconstitute")` | HIGH | `aggregate has reconstitute` |
| 9 | id 필드 final | 빌트인 `beFinal()` | MEDIUM | `aggregate id field is final` |
| 10 | Instant 필드 최소 1개 | 커스텀 `haveFieldOfType("java.time.Instant")`(원본서 복사, private) | MEDIUM | `aggregate has instant field` |
| 11 | 외래키는 VO(원시타입 금지) | 빌트인(원본 그대로) | HIGH | `aggregate foreign key is value object` |
| 12 | `..aggregate..` 위치 | 빌트인 `resideInAPackage` | MEDIUM | `aggregate resides in aggregate package` |
| 13 | public 클래스 | 빌트인 `bePublic()` | MEDIUM | `aggregate is public` |
| 14 | final 클래스 금지 | 커스텀 `notBeFinal()`(원본서 복사, private) | MEDIUM | `aggregate is not final` |
| 16 | 외부 레이어 의존 금지 | 빌트인 `dependOnClassesThat().resideInAnyPackage("..application..","..adapter..")` | HIGH | `aggregate does not depend on outer layers` |
| 17 | createdAt: Instant·final | 빌트인(원본 그대로) | MEDIUM | `aggregate createdAt is instant and final` |
| 18 | updatedAt: Instant·not final | 빌트인(원본 그대로) | MEDIUM | `aggregate updatedAt is instant and not final` |

**버릴 규칙:** #1(lombok), #2(jpa), #3(spring) — 어노테이션 금지. #15 — soft(경고-only). #19~22 — fixture 검사 규칙.

**픽스처 설계:** compliant 1개(`PureAggregate` — 위 모든 규칙 통과: public non-final record-아닌 class, private 생성자, `forNew`/`of`/`reconstitute` static, `final Long id`(VO면 `OrderId`), `Instant createdAt`(final)/`Instant updatedAt`(non-final)) + 카테고리 규칙별 violation 픽스처(각 규칙 1개씩, 의도한 규칙만 위반). 픽스처 패키지에 `.aggregate.` 세그먼트 포함. 이름에 Fixture/Mother/Test 금지. 원본의 커스텀 ArchCondition(`notHaveSetterMethods`/`haveFieldOfType`/`notBeFinal`)은 원본 파일에서 본문을 복사해 `AggregateRules`에 `private static`으로 둔다.

**테스트:** `AggregateRulesTest`가 `AggregateRules.rules().get(key).rule()`을 `Runner.check`로 self-test(compliant 통과 + 각 violation 픽스처가 해당 규칙만 위반). `build.gradle` 변경 불필요(domain-rules는 이미 test에 runtime·junit·archunit 의존).

**커밋:** `git add domain-rules/.../AggregateRules.java domain-rules/.../AggregateRulesTest.java domain-rules/.../fixture/**/aggregate/ && git commit -m "feat(domain-rules): aggregate 규칙 추출"`

**완료 기준:** `./gradlew :domain-rules:test --tests '*AggregateRulesTest' -q` 통과. (이 시점엔 DomainRules에 합류 전이라 전체 getRules엔 없음 — Task 5에서 합류.)

---

## Task 2: ExceptionRules (병렬)

**원본:** `/Users/ryu-qqq/Documents/ryu-qqq/connectly-services/services/crawling/crawling-domain/src/test/java/com/connectly/crawling/domain/architecture/exception/ExceptionArchTest.java`

**Files:**
- Create: `domain-rules/src/main/java/com/ryuqqq/archrules/domain/ExceptionRules.java`
- Create: `domain-rules/src/test/java/com/ryuqqq/archrules/domain/ExceptionRulesTest.java`
- Create: 픽스처 `.../fixture/{compliant,violation}/domain/exception/*`

**Interfaces:**
- Consumes: `ArchConditions.haveMethodWithName(String)` (Task 0), `Runner.check` (test).
- Produces: `ExceptionRules.rules()` → `Map<String,ArchRuleSpec>`.

**상대 매처:** exception 패키지 = `..exception..`.

**남길 규칙 (큐레이션):**

| 원본# | 의도 | 빌트인/커스텀 | priority | `.as` 이름 |
|---|---|---|---|---|
| 1 | ErrorCode enum이 `ErrorCode` 인터페이스 구현 | 커스텀 `implementErrorCodeInterface()`(원본서 복사, 이름기반) | HIGH | `errorcode implements ErrorCode interface` |
| 2 | ErrorCode enum `..exception..` 위치 | 빌트인 | MEDIUM | `errorcode resides in exception package` |
| 4 | ErrorCode enum public | 빌트인 `bePublic()` | MEDIUM | `errorcode is public` |
| 5 | `getCode()` 존재 | `ArchConditions.haveMethodWithName("getCode")` | HIGH | `errorcode has getCode` |
| 6 | `getHttpStatus()` 존재 | `ArchConditions.haveMethodWithName("getHttpStatus")` | HIGH | `errorcode has getHttpStatus` |
| 7 | `getMessage()` 존재 | `ArchConditions.haveMethodWithName("getMessage")` | HIGH | `errorcode has getMessage` |
| 8 | `getHttpStatus()` 반환타입이 Spring 아님 | 커스텀 `haveGetHttpStatusWithValidReturnType()`(원본서 복사) | HIGH | `errorcode getHttpStatus returns non-spring` |
| 9 | Concrete Exception이 `DomainException` 상속 | 커스텀 `extendDomainException()`(원본서 복사, 이름기반) | HIGH | `exception extends DomainException` |
| 10 | Concrete Exception `..exception..` 위치 | 빌트인 | MEDIUM | `exception resides in exception package` |
| 14 | Concrete Exception public | 빌트인 `bePublic()` | MEDIUM | `exception is public` |
| 15 | Concrete가 RuntimeException 상속 | 빌트인 `beAssignableTo(RuntimeException.class)` | HIGH | `exception extends RuntimeException` |
| 18 | 외부 레이어 의존 금지 | 빌트인 `dependOnClassesThat().resideInAnyPackage("..application..","..adapter..")` | HIGH | `exception does not depend on outer layers` |

**버릴 규칙:** #3·11(lombok), #12(jpa), #13(spring) — 어노테이션. #16·17 — DomainException 위치/상속(`..domain.common.exception` 중간 세그먼트 하드코딩, 이식성 모호). #19 — 접근 제한(`onlyBeAccessed`, 하드코딩 위험). #20 — soft(ArchRule 아님, println).

**픽스처 설계:** ErrorCode enum compliant(`CleanErrorCode` — `ErrorCode` 인터페이스 구현 + getCode/getHttpStatus(non-spring 반환)/getMessage + public) + Concrete exception compliant(`CleanException extends DomainException`) + 규칙별 violation. **stub 정의 필요**: 픽스처 패키지에 `interface ErrorCode {}`와 `class DomainException extends RuntimeException {}` stub을 두어 외부 의존 없이 self-test(원본도 이름 기반 비교라 stub로 충분). 픽스처 패키지에 `.exception.` 세그먼트 포함. 원본 커스텀 ArchCondition 3개(`implementErrorCodeInterface`/`extendDomainException`/`haveGetHttpStatusWithValidReturnType`) 본문을 `ExceptionRules`에 `private static`으로 복사.

**테스트:** `ExceptionRulesTest`가 `ExceptionRules.rules()`로 self-test.

**커밋:** `git commit -m "feat(domain-rules): exception 규칙 추출"`

**완료 기준:** `./gradlew :domain-rules:test --tests '*ExceptionRulesTest' -q` 통과.

---

## Task 3: CriteriaRules (병렬)

**원본:** `/Users/ryu-qqq/Documents/ryu-qqq/connectly-services/services/crawling/crawling-domain/src/test/java/com/connectly/crawling/domain/architecture/query/CriteriaArchTest.java`

**Files:**
- Create: `domain-rules/src/main/java/com/ryuqqq/archrules/domain/CriteriaRules.java`
- Create: `domain-rules/src/test/java/com/ryuqqq/archrules/domain/CriteriaRulesTest.java`
- Create: 픽스처 `.../fixture/{compliant,violation}/domain/query/criteria/*`

**Interfaces:**
- Consumes: `ArchConditions.beRecords()` / `haveStaticMethodWithName(String)` (Task 0), `Runner.check`.
- Produces: `CriteriaRules.rules()` → `Map<String,ArchRuleSpec>`.

**상대 매처:** criteria 패키지 = `..query.criteria..`.

**남길 규칙 (큐레이션):**

| 원본# | 의도 | 빌트인/커스텀 | priority | `.as` 이름 |
|---|---|---|---|---|
| 1 | `*Criteria`는 `..query.criteria..` 위치 | 빌트인 | MEDIUM | `criteria resides in criteria package` |
| 2 | criteria는 record | `ArchConditions.beRecords()` | HIGH | `criteria is record` |
| 3 | 이름 `*Criteria`로 끝남 | 빌트인 `haveSimpleNameEndingWith` | MEDIUM | `criteria name ends with Criteria` |
| 4 | `of()` 정적팩토리 | `ArchConditions.haveStaticMethodWithName("of")` | HIGH | `criteria has of` |
| 8 | 외부 레이어 의존 금지(**상대매처 전환**) | 빌트인 `dependOnClassesThat().resideInAnyPackage("..application..","..adapter..")` | HIGH | `criteria does not depend on outer layers` |
| 10 | public 클래스 | 빌트인 `bePublic()` | MEDIUM | `criteria is public` |

**버릴 규칙:** #5(lombok), #6(jpa), #7(spring) — 어노테이션. #9 — 문서용(항상 통과).

> 원본 #8은 `com.connectly.crawling.domain` 화이트리스트를 하드코딩한다. 추출본은 의미를 뒤집어 **블랙리스트**(`..application..`/`..adapter..` 의존 금지)로 상대 매처화한다 — 도메인 순수성 의도는 동일하되 root 무관.

**픽스처 설계:** compliant `SearchCriteria`(record + public + of() + 이름 *Criteria + 외부의존 없음) + 규칙별 violation(record 아님/of 없음/이름 안 끝남/외부레이어 의존 등). 픽스처 패키지에 `.query.criteria.` 세그먼트 포함.

**테스트:** `CriteriaRulesTest`가 `CriteriaRules.rules()`로 self-test.

**커밋:** `git commit -m "feat(domain-rules): criteria 규칙 추출"`

**완료 기준:** `./gradlew :domain-rules:test --tests '*CriteriaRulesTest' -q` 통과.

---

## Task 4: PackageRules (병렬)

**원본:** `/Users/ryu-qqq/Documents/ryu-qqq/connectly-services/services/crawling/crawling-domain/src/test/java/com/connectly/crawling/domain/architecture/PackageStructureArchTest.java`

**Files:**
- Create: `domain-rules/src/main/java/com/ryuqqq/archrules/domain/PackageRules.java`
- Create: `domain-rules/src/test/java/com/ryuqqq/archrules/domain/PackageRulesTest.java`
- Create: 픽스처 `.../fixture/{compliant,violation}/domain/...`(아래 규칙별 패키지)

**Interfaces:**
- Consumes: `Runner.check` (test). (공용 ArchConditions 불필요 — 전부 빌트인.)
- Produces: `PackageRules.rules()` → `Map<String,ArchRuleSpec>`.

**남길 규칙 (큐레이션):**

| 원본# | 의도 | 빌트인 | priority | `.as` 이름 |
|---|---|---|---|---|
| 2 | `..common.util..`은 인터페이스만 | `beInterfaces()` | MEDIUM | `domain common util is interface only` |
| 3 | DomainEvent 구현체는 `..event..` 위치 | `implement` + `resideInAPackage("..event..")` | MEDIUM | `domain event resides in event package` |
| 4 | DomainException 서브타입은 `..exception..` 위치 | `areAssignableTo` + `resideInAPackage("..exception..")` | MEDIUM | `domain exception resides in exception package` |
| 5 | BC 간 순환 의존 금지 | `SlicesRuleDefinition.slices().matching("..domain.(*)..").should().beFreeOfCycles()` | HIGH | `bounded contexts are cycle-free` |

**버릴 규칙:** #1(common 이름 규약 — connectly 특화), #6(common 접근 — 절대 패키지 열거), #7(BC 내부 의존 — 허용목록 하드코딩).

> 규칙 3·4는 `DomainEvent`/`DomainException` 마커가 필요하다. 픽스처 패키지에 `interface DomainEvent {}` / `class DomainException extends RuntimeException {}` stub을 정의해 이름·상속 기반으로 self-test(외부 의존 0). 원본이 인터페이스/상속을 어떻게 식별하는지(이름 vs 타입) 확인해 동일하게 구현.

**픽스처 설계:** 규칙별 compliant/violation. 규칙 2는 `..common.util..` 패키지에 인터페이스(compliant)/클래스(violation). 규칙 3은 `..event..` 안/밖의 DomainEvent 구현체. 규칙 4는 `..exception..` 안/밖의 DomainException 서브타입. 규칙 5는 순환 없는/있는 두 BC 패키지(`..domain.foo..`↔`..domain.bar..`). 각 픽스처가 해당 규칙만 건드리도록 패키지를 분리. 이름에 Fixture/Mother/Test 금지.

**테스트:** `PackageRulesTest`가 `PackageRules.rules()`로 self-test. (규칙 5 순환 검사는 `Runner.check`로 두 BC 픽스처를 함께 import해 평가.)

**커밋:** `git commit -m "feat(domain-rules): package 구조 규칙 추출"`

**완료 기준:** `./gradlew :domain-rules:test --tests '*PackageRulesTest' -q` 통과.

---

## Task 5: 통합 — DomainRules에 4 카테고리 병합

**Files:**
- Modify: `domain-rules/src/main/java/com/ryuqqq/archrules/domain/DomainRules.java`

**Interfaces:**
- Consumes: `AggregateRules.rules()`/`ExceptionRules.rules()`/`CriteriaRules.rules()`/`PackageRules.rules()` (Task 1~4).

- [ ] **Step 1: getRules()에 4 카테고리 병합**

`DomainRules.getRules()`의 `all.putAll(ConnectlyVoRules.rules());` 다음에 추가:
```java
        all.putAll(AggregateRules.rules());
        all.putAll(ExceptionRules.rules());
        all.putAll(CriteriaRules.rules());
        all.putAll(PackageRules.rules());
```

- [ ] **Step 2: 전체 빌드 — 통합 검증**

Run: `./gradlew build -q`
Expected: BUILD SUCCESSFUL. `RuleConventionTest`가 이제 전 카테고리 규칙(도메인2+VO보편3+특화3+aggregate14+exception11+criteria6+package4)의 `.as`/priority/`.because` 컨벤션을 강제. ServiceLoader로 발견되는 규칙 수가 늘어도 self-test는 `XxxRules.rules()` 직접 평가라 상호 간섭 없음.

- [ ] **Step 3: 커밋**

```bash
git add domain-rules/src/main/java/com/ryuqqq/archrules/domain/DomainRules.java
git commit -m "feat(domain-rules): 4개 카테고리 규칙을 DomainRules에 병합"
```

---

## Task 6: purity → hexagonal-rules framework-free 확장 (병렬 가능)

**Files:**
- Modify: `hexagonal-rules/src/main/java/com/ryuqqq/archrules/hexagonal/HexagonalRules.java`
- Create: `hexagonal-rules/src/test/java/com/ryuqqq/archrules/hexagonal/fixture/violation/domain/LocalDateTimeDomain.java`
- Modify: `hexagonal-rules/src/test/java/com/ryuqqq/archrules/hexagonal/HexagonalRulesTest.java`

**Interfaces:**
- Consumes: `Runner.check` (test).
- Produces: 확장된 `HexagonalRules.DOMAIN_FRAMEWORK_FREE`.

- [ ] **Step 1: 실패 테스트 — LocalDateTime 의존 픽스처가 잡혀야**

violation 픽스처 `hexagonal-rules/src/test/java/com/ryuqqq/archrules/hexagonal/fixture/violation/domain/LocalDateTimeDomain.java`:
```java
package com.ryuqqq.archrules.hexagonal.fixture.violation.domain;

import java.time.LocalDateTime;

/** 도메인이 java.time.LocalDateTime에 의존 → 확장된 framework-free 위반. */
public final class LocalDateTimeDomain {
    private final LocalDateTime when = LocalDateTime.MIN;
    public LocalDateTime when() { return when; }
}
```

`HexagonalRulesTest.java`에 테스트 메서드 추가(기존 테스트 클래스의 `Runner.check` 패턴을 따른다 — 기존 `SpringCoupledDomain` 케이스와 동형):
```java
    @Test
    void localDateTimeInDomainViolatesFrameworkFree() {
        assertTrue(com.ryuqqq.archrules.runtime.Runner.check(
                HexagonalRules.DOMAIN_FRAMEWORK_FREE,
                com.ryuqqq.archrules.hexagonal.fixture.violation.domain.LocalDateTimeDomain.class)
                .hasViolation());
    }
```
(import·assertTrue 스타일은 기존 `HexagonalRulesTest`를 따른다. 기존 테스트 파일을 먼저 읽어 동일 패턴 적용.)

- [ ] **Step 2: 테스트 실행 → 실패 확인**

Run: `./gradlew :hexagonal-rules:test --tests '*HexagonalRulesTest' -q`
Expected: FAIL — `LocalDateTime`이 아직 금지목록에 없어 위반 미검출(`hasViolation()`=false).

- [ ] **Step 3: DOMAIN_FRAMEWORK_FREE 금지목록 확장**

`HexagonalRules.java`의 `DOMAIN_FRAMEWORK_FREE`에서 `resideInAnyPackage(...)` 인자 목록을 확장:
```java
                    .resideInAnyPackage(
                            "org.springframework..", "jakarta..", "org.hibernate..",
                            "com.fasterxml.jackson..", "org.junit..",
                            "org.apache.commons..", "com.google.common..", "io.vavr..",
                            "com.google.gson..", "org.slf4j..", "ch.qos.logback..",
                            "org.apache.logging.log4j..", "java.time.LocalDateTime")
```
(`because` 문구도 "프레임워크·유틸 라이브러리 비의존"으로 보강 가능 — 선택.)

- [ ] **Step 4: 테스트 실행 → 통과 확인**

Run: `./gradlew :hexagonal-rules:test --tests '*HexagonalRulesTest' -q`
Expected: PASS — `LocalDateTimeDomain`이 위반으로 검출. 기존 케이스도 통과.

> 나머지 7개 추가 패키지(commons/guava/vavr/gson/slf4j/logback/log4j)는 동일한 `resideInAnyPackage` 메커니즘이므로 LocalDateTime 1건으로 확장 동작이 증명된다. 이들 라이브러리는 클래스패스에 없어(외부 의존 0) 별도 픽스처를 두지 않는다 — `log()`로 남기지 않아도 plan에 근거가 명시됨.

- [ ] **Step 5: 전체 빌드 회귀**

Run: `./gradlew build -q`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: 커밋**

```bash
git add hexagonal-rules/
git commit -m "feat(hexagonal-rules): framework-free에 유틸/로깅 라이브러리·LocalDateTime 금지 추가(purity 흡수)"
```

---

## Self-Review

**1. Spec coverage:** spec §3 카테고리별 추출 대상 — aggregate(Task1)·exception(Task2)·criteria(Task3)·package(Task4)·purity(Task6). §1 모듈 병합(Task0). §4 rules() 병합 패턴(Task0+5). §5 작업 순서(Task0→병렬1-4→통합5, 6 병렬) 일치. 큐레이션 제외 목록(어노테이션/soft/fixture/중복)이 각 task 「버릴 규칙」에 반영. 갭 없음.

**2. Placeholder scan:** Task 0·6은 완전 코드. Task 1~4는 「카테고리 task 작업 방식」에 따라 원본 참조 변환 — 큐레이션 표(규칙별 빌트인/커스텀·priority·이름)와 본보기(VoRules)로 완전 명세. "적절히"·TBD 없음. 원본의 빌트인 호출/커스텀 ArchCondition 본문은 원본 파일에 실재(에이전트가 읽음).

**3. Type consistency:** `XxxRules.rules()` 시그니처가 Task0(Vo/ConnectlyVo)·Task1~4·Task5에서 동일(`static Map<String,ArchRuleSpec> rules()`). `ArchConditions.*` 메서드명이 Task0 정의와 Task1~3 호출에서 일치. `DomainRules.getRules()` 병합 호출(Task5)이 Task1~4 산출 `rules()`와 일치. self-test는 `XxxRules.rules().get(key).rule()` 패턴 통일.

**범위 밖:** connectly-services 인라인 룰 제거·소비(#3), adapter/application 카테고리.
