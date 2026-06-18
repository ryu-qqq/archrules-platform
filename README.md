# archrules-platform

폴리레포에 공통 [ArchUnit](https://www.archunit.org/) 규칙을 배포·적용하고 위반을 CI에서 가시화하는, **외부 빌드 의존 0**의 경량 플랫폼. Netflix Nebula ArchRules 스타일(SPI로 규칙 저작 → ServiceLoader 발견 → 실행·리포트)이되, Gradle 플러그인·variant 같은 무거운 기계장치 없이 직접 구현했다.

- **좌표**: `com.github.ryu-qqq.archrules-platform:<module>:<tag>` (JitPack)
- **현재**: `0.1.0` — Phase 0·1 + Phase 2 slice1(소비 메커니즘) 완료
- **런타임 의존**: ArchUnit + 우리 SPI 뿐 (Nebula·Spring 등 0)

---

## 모듈

| 모듈 | 역할 |
|------|------|
| **archrules-api** | SPI. 규칙 라이브러리가 구현하는 인터페이스(`ArchRulesService`)와 규칙 디스크립터(`ArchRuleSpec`). ArchUnit에만 의존. |
| **archrules-runtime** | 규칙 발견·실행·리포트·CI 진입점. `ServiceLoader`로 규칙을 모아 소비자 클래스에 평가. |
| **hexagonal-rules** | 규칙 라이브러리 — 헥사고날 경계(레이어 방향·프레임워크 비의존). |
| **domain-rules** | 규칙 라이브러리 — 도메인 작성 컨벤션. |

`archrules-api ← archrules-runtime`(api 의존) / `archrules-api ← {hexagonal,domain}-rules`(api만 의존, plain jar + `META-INF/services` 등록). 규칙 라이브러리는 runtime에 의존하지 않는다(테스트에서만).

---

## archrules-api (SPI)

### `ArchRulesService`
규칙 라이브러리가 구현하는 단일 진입점. `ServiceLoader`로 발견된다.

```java
@FunctionalInterface
public interface ArchRulesService {
    Map<String, ArchRuleSpec> getRules();   // 키 = 규칙 이름
}
```

### `ArchRuleSpec`
규칙 1건 = ArchUnit `ArchRule` + `Priority`. ArchUnit 1.2.1의 `ArchRule`은 priority를 노출하지 않으므로 여기 함께 싣는다(단일 출처).

```java
public record ArchRuleSpec(ArchRule rule, Priority priority) { /* rule·priority non-null */ }
```

### 규칙 작성 컨벤션 (강제)
모든 노출 규칙은 **priority + `.as(name)` + `.because(reason)`** 를 갖는다. priority는 `ArchRuleSpec`에 싣고 ArchUnit 빌더의 `priority(...)` 접두는 쓰지 않는다. 각 규칙 라이브러리의 `RuleConventionTest`가 이를 self-test로 강제한다. 상대 패키지 매처(`..domain..` 등)만 쓰고 root 패키지를 하드코딩하지 않는다(이식성).

예시:
```java
public final class MyRules implements ArchRulesService {
    static final ArchRule NO_TIME =
        noClasses().that().resideInAPackage("..domain..")
            .should().callMethodWhere(/* ... */)
            .as("domain reads no clock")
            .because("도메인은 현재 시각을 직접 읽지 않고 주입받는다")
            .allowEmptyShould(true);

    @Override public Map<String, ArchRuleSpec> getRules() {
        return Map.of("domain reads no clock", new ArchRuleSpec(NO_TIME, Priority.HIGH));
    }
}
```
+ `src/main/resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService` 에 구현 FQN 한 줄 등록.

---

## archrules-runtime

### `Runner` — self-test 헬퍼
`ClassFileImporter` 보일러플레이트 없이 클래스 참조만으로 규칙을 평가. 규칙 라이브러리 self-test에서 사용.

```java
public static EvaluationResult check(ArchRule rule, Class<?>... classes);
// 예) assertFalse(Runner.check(rule, CompliantFixture.class).hasViolation());
```

### `ArchRulesRunner` — 발견 + 평가
클래스패스의 모든 `ArchRulesService`를 `ServiceLoader`로 발견해 각 규칙을 평가, `RuleResult` 목록 반환.

```java
public static List<RuleResult> run(JavaClasses classes);   // 이미 import된 클래스
public static List<RuleResult> run(Path classesDir);       // 파일시스템 경로 import (CI용)
```
분석 대상(소비자 클래스)은 `run(Path)`로 importPath, 규칙은 ServiceLoader 클래스패스에서 — 둘을 분리한다.

### `RuleResult`
```java
public record RuleResult(String ruleName, Priority priority, boolean hasViolation, List<String> violations) {}
```

### `ArchRulesReport` — markdown 리포트
규칙별 `| Rule | Priority | Status | Violations |` 요약 + 위반 상세. 위반 메시지의 `|`·개행은 이스케이프. 0 규칙이면 "No rules" 안내.

```java
public static String toMarkdown(List<RuleResult> results);
```

### `ArchRulesCli` — CI 진입점
컴파일된 소비자 클래스에 규칙을 직접 실행하고 markdown 리포트를 쓴다.

```bash
java -cp <runtime+규칙 jar> com.ryuqqq.archrules.runtime.ArchRulesCli \
  --classes build/classes/java/main \
  --report  build/reports/archrules/report.md \
  --threshold HIGH        # HIGH|MEDIUM|LOW|none(기본=report-only)
```

**종료 코드 (게이트 의미):**
| exit | 의미 |
|------|------|
| `2` | 규칙 0개 발견 — **silent-skip 가드**(규칙 의존 누락/미적용 감지) |
| `1` | `--threshold` 이상 우선순위의 위반 발생 |
| `0` | 그 외(report-only 포함) |

우선순위 서열은 ArchUnit `Priority` 기준 **HIGH < MEDIUM < LOW**. threshold "이상"은 `priority.compareTo(threshold) <= 0`(같거나 더 강함). `--threshold` 없으면 위반이 있어도 빌드를 막지 않는다(report-only) — 도입 초기 채택 저항 최소화.

### 브라운필드 ratchet (기존 위반 동결, 신규만 실패)

기존 코드베이스의 위반을 즉시 제거할 수 없을 때, `--baseline` 플래그로 현재 위반을 "동결"하고 **신규 위반만** 규칙 게이트에 걸릴 수 있다.

```bash
java -cp <runtime+규칙 jar> com.ryuqqq.archrules.runtime.ArchRulesCli \
  --classes  build/classes/java/main \
  --report   build/reports/archrules/report.md \
  --baseline .archrules/baseline \
  --threshold HIGH
```

`--baseline <dir>`이 지정되면:
1. 첫 실행: store 디렉토리가 없으면 **현재 위반을 baseline으로 저장**하고 통과(exit 0)
2. 이후 실행: baseline과 비교해 **store에 없던 신규 위반만** threshold 게이트에 걸림
3. store는 소비 레포가 소유하며 git에 커밋해 ratchet을 유지

---

## 소비 (폴리레포 적용)

소비 레포는 **자기 build를 수정하지 않는다.** 공유 reusable GitHub Actions 워크플로우가 `--init-script`로 `ci/archrules.init.gradle`을 주입해, 소비자 root 프로젝트에 규칙 jar를 해석하는 `archrulesClasspath` configuration과 `archRulesCheck` JavaExec 태스크를 더한다.

소비 레포 워크플로우(한 줄 참조):
```yaml
jobs:
  archrules:
    uses: ryu-qqq/archrules-platform/.github/workflows/archrules-reusable.yml@0.1.0
    with:
      classes-dir: build/classes/java/main
      threshold: none          # HIGH|MEDIUM|LOW|none
      rules-version: "0.1.0"
```
리포트는 `$GITHUB_STEP_SUMMARY`(잡 요약) + 아티팩트로 표면화된다. 규칙이 0개 발견되면(의존 누락 등) exit 2로 잡이 실패해 "규칙이 배포됐는데 적용 안 한 채 그린"을 구조적으로 막는다.

---

## 빌드 · 테스트

```bash
./gradlew build              # 전 모듈 빌드 + self-test
./gradlew publishToMavenLocal   # 로컬 발행 (com.github.ryu-qqq.archrules-platform:*:0.1.0)
```
JitPack은 git 태그를 요청 시 빌드한다 — `com.github.ryu-qqq.archrules-platform:<module>:<tag>`.

---

## 현재 규칙

**hexagonal-rules**
| 규칙 | priority | 의미 |
|------|----------|------|
| `domain is framework-free` | HIGH | `..domain..` 은 Spring/JPA/Hibernate/Jackson 등에 의존 안 함 |
| `application avoids web/persistence` | HIGH | `..application..` 은 웹/영속 스택에 직접 의존 안 함(포트로만) |
| `hexagonal layer direction` | MEDIUM | 의존은 안쪽으로만(adapter→application→domain, bootstrap만 조립 루트) |

**domain-rules**
| 규칙 | priority | 의미 |
|------|----------|------|
| `domain reads no clock` | HIGH | 도메인은 `Instant.now()` 등 현재 시각을 직접 읽지 않고 주입받음 |
| `domain has no setters` | MEDIUM | 도메인 상태 변경은 `set*` 가 아니라 비즈니스 메서드로 |

> 도메인 작성 컨벤션 규칙은 추가 예정(aggregate/vo/id/errorcode/sortkey 등). 로드맵 참고.

---

## 로드맵

- **라이브 e2e**: throwaway 소비 레포 + reusable 워크플로우 Actions 실행 확인
- **규칙 확장**: domain-rules 나머지 컨벤션 규칙 이식(aggregate is class·ctors not public·VO 필드·id record·errorcode/sortkey enum·package slices), base 클래스 설정형 규칙(예외 베이스)
- **운영**: org 존재 린트(모든 소비 레포가 워크플로우를 참조하는지 감사), 멀티모듈 `--classes` 지원

---

## 설계 메모

- **Nebula 비채택**: `nebula-archrules-plugin`의 코어(SPI+ServiceLoader+self-test 헬퍼+report-only 게이트)는 흡수하되, Gradle 플러그인·`arch-rules` variant·work-action 격리는 우리 규모에 불필요해 걷어냈다. 규칙은 우리 SPI에만 의존해 항상 이식 가능.
- **외부 빌드 의존 0**: ArchUnit·JUnit 외 서드파티 없음.
