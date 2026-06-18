# Frozen Baseline (Ratchet) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** archrules-platform이 브라운필드 레포에서 기존 위반을 baseline으로 동결하고 신규 위반만 실패시키는 ratchet(`FreezingArchRule`)을 지원한다.

**Architecture:** 규칙 라이브러리는 strict 규칙만 노출(현행 유지). freeze 여부는 **런타임 토글**이다 — `ArchRulesRunner.run(..., boolean freeze)`이 freeze=true일 때 각 규칙을 `FreezingArchRule.freeze(rule)`로 감싸 평가한다. violation store 경로·생성정책은 ArchUnit 표준 시스템 프로퍼티(`archunit.freeze.store.default.path`, `archunit.freeze.store.default.allowStoreCreation`)로 제어하며, CLI의 `--baseline <dir>` 플래그가 이를 설정한다. commons처럼 Frozen 전용 클래스를 따로 두지 않아 규칙 중복이 없다.

**Tech Stack:** Java 21, ArchUnit 1.2.1 (`com.tngtech.archunit.library.freeze` — core jar에 포함, 추가 의존 없음), JUnit 5.11.4.

## Global Constraints

- **외부 빌드 의존 0**: ArchUnit·JUnit 외 서드파티 추가 금지. (freeze는 archunit core라 충족)
- **Java 21** toolchain.
- **규칙 라이브러리는 strict 규칙만 노출**한다. freeze는 런타임 결정이며 규칙 라이브러리(hexagonal-rules/domain-rules)는 이 작업에서 변경하지 않는다.
- **하위호환**: 기존 `ArchRulesRunner.run(JavaClasses)` / `run(Path)` / `ArchRulesCli.execute(...)` 시그니처는 유지하고 freeze=false로 위임한다. 기존 테스트가 깨지면 안 된다.
- ArchUnit `Priority` 서열은 HIGH < MEDIUM < LOW (기존 게이트 의미 보존).

---

## File Structure

| 파일 | 책임 | 변경 |
|------|------|------|
| `archrules-runtime/src/main/java/com/ryuqqq/archrules/runtime/ArchRulesRunner.java` | 발견 + 평가. freeze 토글 추가 | Modify |
| `archrules-runtime/src/main/java/com/ryuqqq/archrules/runtime/ArchRulesCli.java` | CLI 진입점. `--baseline` 파싱 + store 시스템 프로퍼티 설정 | Modify |
| `archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/FreezeRunnerTest.java` | Runner freeze 동작 테스트 | Create |
| `archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/freezefixture/one/Banned.java` | baseline 위반 픽스처 (public, simpleName=`Banned`) | Create |
| `archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/freezefixture/two/Banned.java` | 신규 위반 픽스처 (public, simpleName=`Banned`) | Create |
| `README.md` | `--baseline` 사용법·ratchet 설명 추가 | Modify |

> **픽스처 주의:** 기존 `FixtureRulesService` 규칙은 `haveSimpleName("Banned")`에만 매칭한다. 신규 위반을 만들려면 **simpleName이 정확히 `Banned`인 public 클래스 2개**가 서로 다른 컴파일 단위에 필요하다. 따라서 픽스처는 파일명이 아니라 클래스명이 `Banned`여야 한다. 아래 Task 1에서 `freezefixture/one/Banned.java`, `freezefixture/two/Banned.java`로 만든다(패키지로 분리, simpleName 동일).

---

### Task 1: ArchRulesRunner에 freeze 토글 추가

**Files:**
- Modify: `archrules-runtime/src/main/java/com/ryuqqq/archrules/runtime/ArchRulesRunner.java`
- Create: `archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/freezefixture/one/Banned.java`
- Create: `archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/freezefixture/two/Banned.java`
- Create: `archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/FreezeRunnerTest.java`

**Interfaces:**
- Consumes: `ArchRulesService`(ServiceLoader, test resources에 `FixtureRulesService` 등록됨), `ArchRuleSpec.rule()`, `RuleResult`.
- Produces: `ArchRulesRunner.run(JavaClasses, boolean freeze)` 와 `ArchRulesRunner.run(Path, boolean freeze)`. 기존 `run(JavaClasses)`·`run(Path)`는 freeze=false 위임으로 유지.

- [ ] **Step 1: 위반 픽스처 2개 생성**

`freezefixture/one/Banned.java`:
```java
package com.ryuqqq.archrules.runtime.freezefixture.one;

/** "no public Banned" 규칙의 위반 픽스처 (baseline 대상). */
public class Banned {}
```

`freezefixture/two/Banned.java`:
```java
package com.ryuqqq.archrules.runtime.freezefixture.two;

/** baseline 동결 후 추가되는 신규 위반 픽스처. */
public class Banned {}
```

- [ ] **Step 2: 실패 테스트 작성**

`FreezeRunnerTest.java`:
```java
package com.ryuqqq.archrules.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FreezeRunnerTest {

    private void withStore(Path dir, Runnable body) {
        System.setProperty("archunit.freeze.store.default.path", dir.toString());
        System.setProperty("archunit.freeze.store.default.allowStoreCreation", "true");
        try {
            body.run();
        } finally {
            System.clearProperty("archunit.freeze.store.default.path");
            System.clearProperty("archunit.freeze.store.default.allowStoreCreation");
        }
    }

    @Test
    void freeze_firstRun_freezesExistingViolationsAsBaseline(@TempDir Path store) throws Exception {
        JavaClasses baseline = new ClassFileImporter()
                .importClasses(com.ryuqqq.archrules.runtime.freezefixture.one.Banned.class);

        withStore(store, () -> {
            List<RuleResult> results = ArchRulesRunner.run(baseline, true);
            // 첫 실행: 기존 위반을 baseline으로 동결 → 보고되는 위반 0
            assertFalse(results.stream().anyMatch(RuleResult::hasViolation),
                    "freeze 첫 실행은 기존 위반을 동결해 통과시킨다");
        });
        // store 파일이 생성됐다
        assertTrue(Files.list(store).findAny().isPresent(), "violation store 파일 생성");
    }

    @Test
    void freeze_secondRun_failsOnlyOnNewViolation(@TempDir Path store) {
        JavaClasses baselineOnly = new ClassFileImporter()
                .importClasses(com.ryuqqq.archrules.runtime.freezefixture.one.Banned.class);
        JavaClasses baselinePlusNew = new ClassFileImporter()
                .importClasses(
                        com.ryuqqq.archrules.runtime.freezefixture.one.Banned.class,
                        com.ryuqqq.archrules.runtime.freezefixture.two.Banned.class);

        withStore(store, () -> {
            ArchRulesRunner.run(baselineOnly, true);                 // baseline 동결
            List<RuleResult> second = ArchRulesRunner.run(baselinePlusNew, true); // 신규 1개 추가
            assertTrue(second.stream().anyMatch(RuleResult::hasViolation),
                    "baseline에 없던 신규 위반은 실패로 보고된다");
        });
    }
}
```

- [ ] **Step 3: 테스트 실행 → 실패 확인**

Run: `./gradlew :archrules-runtime:test --tests '*FreezeRunnerTest' -q`
Expected: 컴파일 에러 — `run(JavaClasses, boolean)` 메서드 없음.

- [ ] **Step 4: ArchRulesRunner에 freeze 토글 구현**

`ArchRulesRunner.java` 전체를 아래로 교체:
```java
package com.ryuqqq.archrules.runtime;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/** 클래스패스의 모든 {@link ArchRulesService}를 발견해 규칙을 평가한다. */
public final class ArchRulesRunner {

    private ArchRulesRunner() {}

    public static List<RuleResult> run(Path classesDir) {
        return run(classesDir, false);
    }

    /** freeze=true면 각 규칙을 baseline ratchet으로 감싸 평가한다(브라운필드). */
    public static List<RuleResult> run(Path classesDir, boolean freeze) {
        JavaClasses classes = new ClassFileImporter().importPath(classesDir);
        return run(classes, freeze);
    }

    public static List<RuleResult> run(JavaClasses classes) {
        return run(classes, false);
    }

    public static List<RuleResult> run(JavaClasses classes, boolean freeze) {
        List<RuleResult> results = new ArrayList<>();
        for (ArchRulesService service : ServiceLoader.load(ArchRulesService.class)) {
            for (Map.Entry<String, ArchRuleSpec> entry : service.getRules().entrySet()) {
                ArchRuleSpec spec = entry.getValue();
                ArchRule rule = freeze ? FreezingArchRule.freeze(spec.rule()) : spec.rule();
                EvaluationResult eval = rule.evaluate(classes);
                results.add(new RuleResult(
                        entry.getKey(),
                        spec.priority(),
                        eval.hasViolation(),
                        List.copyOf(eval.getFailureReport().getDetails())));
            }
        }
        return results;
    }
}
```

- [ ] **Step 5: 테스트 실행 → 통과 확인**

Run: `./gradlew :archrules-runtime:test --tests '*FreezeRunnerTest' -q`
Expected: PASS (2 테스트).

만약 `StoreReadException`/`allowStoreCreation` 관련 실패가 나면 시스템 프로퍼티 키를 점검한다. ArchUnit 1.2.1에서 store 경로 키는 `archunit.freeze.store.default.path`, 생성 허용 키는 `archunit.freeze.store.default.allowStoreCreation`이다(`archunit.properties`의 `freeze.store.default.*`에 대응하는 시스템 프로퍼티).

- [ ] **Step 6: 전체 runtime 테스트로 회귀 확인**

Run: `./gradlew :archrules-runtime:test -q`
Expected: PASS (기존 `ArchRulesRunnerTest`·`ArchRulesCliTest` 포함 전부 — freeze=false 위임이 하위호환 보존).

- [ ] **Step 7: 커밋**

```bash
git add archrules-runtime/src/main/java/com/ryuqqq/archrules/runtime/ArchRulesRunner.java \
        archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/FreezeRunnerTest.java \
        archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/freezefixture/
git commit -m "feat(runtime): ArchRulesRunner에 freeze(baseline ratchet) 토글 추가"
```

---

### Task 2: ArchRulesCli에 `--baseline` 옵션 추가

**Files:**
- Modify: `archrules-runtime/src/main/java/com/ryuqqq/archrules/runtime/ArchRulesCli.java`
- Modify: `archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/ArchRulesCliTest.java`

**Interfaces:**
- Consumes: `ArchRulesRunner.run(Path, boolean)` (Task 1).
- Produces: `ArchRulesCli.execute(Path classesDir, Path reportOut, Priority threshold, Path baselineDir)` — baselineDir!=null이면 store 시스템 프로퍼티 설정 후 freeze 모드로 실행. 기존 4-인자 `execute(classesDir, reportOut, threshold)`는 baselineDir=null 위임으로 유지. `main`은 `--baseline <dir>` 플래그를 파싱.

- [ ] **Step 1: 실패 테스트 작성 — baseline 첫 실행은 위반을 동결**

`ArchRulesCliTest.java`에 메서드 추가:
```java
    @Test
    void baselineFirstRun_freezesViolations_andExitsZero(@TempDir Path tmp) throws Exception {
        Path classesDir = Path.of("build/classes/java/test");
        Path report = tmp.resolve("report.md");
        Path baseline = tmp.resolve("archunit_store");

        ArchRulesCli.CliOutcome outcome =
                ArchRulesCli.execute(classesDir, report, Priority.HIGH, baseline);

        // baseline 첫 실행: 기존 위반 동결 → 게이트(HIGH)에도 exit 0
        assertEquals(0, outcome.exitCode(), "baseline 첫 실행은 기존 위반을 동결해 통과");
        assertTrue(Files.exists(baseline), "violation store 디렉토리 생성");
        assertTrue(outcome.rulesRun() >= 1, "규칙 발견");
    }
```

- [ ] **Step 2: 테스트 실행 → 실패 확인**

Run: `./gradlew :archrules-runtime:test --tests '*ArchRulesCliTest' -q`
Expected: 컴파일 에러 — 4-인자 `execute` 없음.

- [ ] **Step 3: ArchRulesCli 구현 — execute 오버로드 + store 프로퍼티 + 플래그 파싱**

`ArchRulesCli.java`에서 기존 `execute(...)` 메서드를 아래 두 메서드로 교체:
```java
    public static CliOutcome execute(Path classesDir, Path reportOut, Priority threshold)
            throws Exception {
        return execute(classesDir, reportOut, threshold, null);
    }

    /** baselineDir!=null이면 violation store를 그 경로에 두고 freeze(ratchet) 모드로 실행한다. */
    public static CliOutcome execute(
            Path classesDir, Path reportOut, Priority threshold, Path baselineDir)
            throws Exception {
        boolean freeze = baselineDir != null;
        if (freeze) {
            System.setProperty(
                    "archunit.freeze.store.default.path", baselineDir.toAbsolutePath().toString());
            System.setProperty("archunit.freeze.store.default.allowStoreCreation", "true");
        }
        List<RuleResult> results = ArchRulesRunner.run(classesDir, freeze);
        String md = ArchRulesReport.toMarkdown(results);
        if (reportOut != null) {
            Files.createDirectories(reportOut.toAbsolutePath().getParent());
            Files.writeString(reportOut, md);
        }
        int failures = (int) results.stream().filter(RuleResult::hasViolation).count();
        int exit = mapExitCode(results.size(), gateFailures(results, threshold), threshold);
        return new CliOutcome(exit, md, results.size(), failures);
    }
```

그리고 `main`의 인자 파싱 switch에 `--baseline` 케이스를 추가하고, 로컬 변수와 호출을 갱신한다.
파싱 루프 위에 추가:
```java
        Path baseline = null;
```
switch에 케이스 추가(`--threshold` 케이스 뒤):
```java
                case "--baseline" -> baseline = Path.of(requireValue(args, i++, "--baseline"));
```
`execute` 호출을 4-인자로 교체:
```java
        CliOutcome outcome = execute(classes, report, threshold, baseline);
```

- [ ] **Step 4: 테스트 실행 → 통과 확인**

Run: `./gradlew :archrules-runtime:test --tests '*ArchRulesCliTest' -q`
Expected: PASS (기존 4개 + 신규 1개).

- [ ] **Step 5: 전체 빌드 회귀 확인**

Run: `./gradlew build -q`
Expected: BUILD SUCCESSFUL (전 모듈 self-test 통과).

- [ ] **Step 6: 커밋**

```bash
git add archrules-runtime/src/main/java/com/ryuqqq/archrules/runtime/ArchRulesCli.java \
        archrules-runtime/src/test/java/com/ryuqqq/archrules/runtime/ArchRulesCliTest.java
git commit -m "feat(cli): --baseline 플래그로 freeze ratchet 모드 지원"
```

---

### Task 3: README에 ratchet 사용법 문서화

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: Task 2의 `--baseline <dir>` CLI 플래그.
- Produces: 없음(문서).

- [ ] **Step 1: README의 `ArchRulesCli` 섹션에 `--baseline` 추가**

`README.md`의 CLI 사용 예(``` --threshold HIGH``` 블록 아래)에 다음을 추가:
````markdown
브라운필드 ratchet(기존 위반 동결, 신규만 실패):
```bash
java -cp <runtime+규칙 jar> com.ryuqqq.archrules.runtime.ArchRulesCli \
  --classes  build/classes/java/main \
  --report   build/reports/archrules/report.md \
  --baseline .archrules/baseline \   # violation store 디렉토리 (소비 레포가 git에 커밋·소유)
  --threshold HIGH
```
`--baseline`이 있으면 각 규칙을 `FreezingArchRule`로 감싸, store가 없으면 현재 위반을 baseline으로 **동결**하고(첫 실행 통과), 이후 **store에 없던 신규 위반만** threshold 게이트에 걸린다. store는 소비 레포가 소유하며 git에 커밋해 ratchet을 유지한다.
````

또한 "로드맵"의 브라운필드 항목 `- **브라운필드**: \`FreezingArchRule\` baseline(...)`을 완료 표시로 옮기거나 제거한다(설계 메모와 일관되게).

- [ ] **Step 2: 커밋**

```bash
git add README.md
git commit -m "docs: --baseline ratchet 사용법 추가 + 로드맵 갱신"
```

---

## Self-Review

**1. Spec coverage:** 정본 지도 sub-project #1 = "Frozen baseline 흡수 (`FreezingArchRule` 지원 + CLI/리포트 연동)". Task 1(Runner freeze)·Task 2(CLI `--baseline` + 리포트 경로 유지)·Task 3(문서)이 이를 덮는다. commons의 `HexagonalArchRulesFrozen` 정적 클래스는 **의도적으로** 답습하지 않고 런타임 토글로 대체(Architecture에 명시) → spec의 "흡수" 요구 충족, 중복 회피.

**2. Placeholder scan:** TBD/TODO/"적절히 처리" 없음. 모든 코드 step에 완전한 코드 포함. store 프로퍼티 키 불확실성은 Task1-Step5에 대체 점검 지침으로 명시(placeholder 아님).

**3. Type consistency:** `run(JavaClasses, boolean)`·`run(Path, boolean)`(Task1) → `execute(Path,Path,Priority,Path)`(Task2)에서 일관 사용. `RuleResult::hasViolation`, `CliOutcome` 필드명 기존과 일치. 시스템 프로퍼티 키 문자열 3곳(테스트·CLI) 동일.

**범위 밖(후속 sub-project):** 인라인 룰 추출(#2), connectly-services 소비·store git 운영(#3)은 이 plan에 포함하지 않는다.
