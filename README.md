# archrules-platform

폴리레포에 공통 [ArchUnit](https://www.archunit.org/) 규칙을 **단일 진실의 원천(SSOT)** 으로 배포·적용하고 위반을 CI에서 가시화하는, **외부 빌드 의존 0**의 경량 플랫폼. Netflix [Nebula ArchRules](https://github.com/nebula-plugins/nebula-archrules) 스타일(SPI로 규칙 저작 → ServiceLoader 발견 → 실행·리포트)이되, Gradle 플러그인·variant 같은 무거운 기계장치 없이 직접 구현했다.

- **좌표**: `com.github.ryu-qqq.archrules-platform:<module>:<tag>` (JitPack)
- **현재**: `0.2.0` — 엔진 + C-1~C-7 규칙 세트 + 중립 프리미티브(AppPackages)
- **런타임 의존**: ArchUnit + 우리 SPI 뿐 (Nebula·Spring·jMolecules·Modulith 등 0)
- **포지셔닝/결정 근거**: [`docs/adr/0001-archrules-platform-positioning-and-make-vs-buy.md`](docs/adr/0001-archrules-platform-positioning-and-make-vs-buy.md) — 엔진=Nebula식·룰=어노테이션-프리 상대매처·헥사고날은 자작(jMolecules/Modulith 비채택).

---

## 진실의 원천(SSOT) 원칙

아키텍처 규칙은 **여기 한 곳**에만 둔다. 소비 레포는 규칙을 **복제하지 않는다**:

- ❌ 각 레포가 ArchUnit 테스트를 인라인으로 재작성 → 규칙이 갈라진다.
- ✅ archrules-platform을 의존해 **공통 규칙(C-1~C-7)을 그대로 받고**, 그 레포 도메인에만 필요한 규칙은 **우리 엔진(`ArchRulesService`) 위에 자체 추가**한다(Nebula "사내 내부 라이브러리" 모델).

소비 경로는 둘이다:
1. **단독 JitPack** — `com.github.ryu-qqq:archrules-platform:<module>:<tag>` 직접 의존.
2. **공통 SDK 번들** — `spring-platform-commons`에 묶어 그 SDK를 쓰는 서비스가 규칙을 자동으로 받음(번들도 JitPack 게시). *(로드맵)*

---

## 모듈

**엔진**
| 모듈 | 역할 |
|------|------|
| **archrules-api** | SPI. 규칙 라이브러리가 구현하는 인터페이스(`ArchRulesService`) + 규칙 디스크립터(`ArchRuleSpec`). ArchUnit에만 의존. |
| **archrules-runtime** | 규칙 발견·실행·리포트·CI 진입점. `ServiceLoader`로 규칙을 모아 소비자 클래스에 평가. baseline ratchet. |
| **archrules-common** | 도메인/컨텍스트 비종속 **중립 프리미티브**(`AppPackages` — 서드파티 false-positive 차단용 앱-베이스 판정). 규칙 라이브러리가 공용으로 의존. |

**규칙 라이브러리 (C-1~C-7)**
| 모듈 | 규칙 그룹 |
|------|------|
| **domain-rules** | C-1 도메인 순수성 — 시각 비의존·setter 금지·private 생성자+정적팩토리·VO·aggregate·exception·criteria 컨벤션. |
| **hexagonal-rules** | C-2 헥사고날 경계 — 레이어 방향(domain←application←adapter←bootstrap)·프레임워크 비의존. |
| **context-isolation-rules** | C-3 컨텍스트 격리 — 다른 컨텍스트 internal 직접 import 금지, 교차는 `.api`/이벤트로만, 코어는 외부 `.api`도 비의존. |
| **security-boundary-rules** | C-4 보안 경계 — authhub는 gateway만 의존, public 어댑터 DTO 도메인 비노출. |
| **messaging-rules** | C-5 메시징 — 이벤트 payload는 도메인 애그리거트가 아니라 published DTO. |
| **persistence-rules** | C-6 영속 — JPA 엔티티가 영속 어댑터 밖으로 누출 금지. |
| **shared-kernel-rules** | C-7 shared-kernel — 어떤 컨텍스트도 import 안 함(역의존 0). |

의존: `archrules-api ← archrules-runtime` / `archrules-api(+archrules-common) ← 규칙 라이브러리`(plain jar + `META-INF/services` 등록). 규칙 라이브러리는 runtime에 의존하지 않는다(테스트에서만).

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
모든 노출 규칙은 **priority + `.as(name)` + `.because(reason)` + `.allowEmptyShould(true)`** 를 갖는다. priority는 `ArchRuleSpec`에 싣는다. 각 규칙 라이브러리의 `RuleConventionTest`가 self-test로 강제한다. **상대 패키지 매처(`..domain..` 등)만** 쓰고 root 패키지를 하드코딩하지 않는다(이식성·어노테이션-프리).

---

## 도메인 특화 규칙 추가 (소비 레포에서)

소비 레포는 자기 도메인에만 필요한 규칙을 **우리 엔진 위에** 둔다 — 공통 규칙을 건드리지 않는다.

```java
// (소비 레포) src/.../MyServiceRules.java
public final class MyServiceRules implements com.ryuqqq.archrules.api.ArchRulesService {
    static final ArchRule X = noClasses().that().resideOutsideOfPackage("..gateway..")
            .should().dependOnClassesThat().resideInAPackage("..authhub..")
            .as("only gateway depends on authhub")
            .because("인증은 엣지에서만").allowEmptyShould(true);

    @Override public Map<String, ArchRuleSpec> getRules() {
        return Map.of("only gateway depends on authhub", new ArchRuleSpec(X, Priority.HIGH));
    }
}
```
+ `src/.../resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService` 에 FQN 한 줄. ServiceLoader가 공통 규칙과 함께 자동 발견한다.

---

## archrules-runtime

### `Runner` — self-test 헬퍼
```java
public static EvaluationResult check(ArchRule rule, Class<?>... classes);
// 예) assertFalse(Runner.check(rule, CompliantFixture.class).hasViolation());
```

### `ArchRulesRunner` — 발견 + 평가
클래스패스의 모든 `ArchRulesService`를 `ServiceLoader`로 발견해 평가, `RuleResult` 목록 반환.
```java
public static List<RuleResult> run(JavaClasses classes);   // 이미 import된 클래스
public static List<RuleResult> run(Path classesDir);       // 파일시스템 경로 import (CI용)
```

### `ArchRulesCli` — CI 진입점
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

우선순위 서열은 ArchUnit `Priority` 기준 **HIGH < MEDIUM < LOW**. `--threshold` 없으면 위반이 있어도 빌드를 막지 않는다(report-only) — 도입 초기 채택 저항 최소화.

### 브라운필드 ratchet (기존 위반 동결, 신규만 실패)
기존 코드베이스의 위반을 즉시 못 없앨 때 `--baseline <dir>`로 현재 위반을 동결하고 **신규 위반만** 게이트에 건다.
```bash
java -cp <...> com.ryuqqq.archrules.runtime.ArchRulesCli \
  --classes build/classes/java/main --report build/reports/archrules/report.md \
  --baseline .archrules/baseline --threshold HIGH
```
1. 첫 실행: store가 없으면 현재 위반을 baseline으로 저장하고 통과(exit 0). 2. 이후: store에 없던 신규 위반만 게이트. 3. store는 소비 레포가 소유·커밋.

---

## 소비 (폴리레포 적용)

소비 레포는 **자기 build를 수정하지 않는다.** 공유 reusable GitHub Actions 워크플로우가 `--init-script`로 `ci/archrules.init.gradle`을 주입해, root 프로젝트에 규칙 jar를 해석하는 `archrulesClasspath` configuration과 `archRulesCheck` JavaExec 태스크를 더한다.

```yaml
jobs:
  archrules:
    uses: ryu-qqq/archrules-platform/.github/workflows/archrules-reusable.yml@0.2.0
    with:
      classes-dir: build/classes/java/main
      threshold: none          # HIGH|MEDIUM|LOW|none
      rules-version: "0.2.0"
```
리포트는 `$GITHUB_STEP_SUMMARY` + 아티팩트로 표면화. 규칙이 0개 발견되면 exit 2로 잡 실패 → "규칙이 배포됐는데 적용 안 한 채 그린"을 구조적으로 막는다.

---

## 현재 규칙 (C-1 ~ C-7)

**domain-rules (C-1)** — `domain reads no clock`(HIGH) · `domain has no setters`(MEDIUM) + aggregate/VO/id/exception/criteria 컨벤션
**hexagonal-rules (C-2)** — `domain is framework-free`(HIGH) · `application avoids web/persistence`(HIGH) · `hexagonal layer direction`(MEDIUM)
**context-isolation-rules (C-3)** — `no cross-context internals`(HIGH) · `core blind to foreign api`(HIGH)
**security-boundary-rules (C-4)** — `only gateway depends on authhub`(HIGH) · `public adapter exposes no domain`(HIGH)
**messaging-rules (C-5)** — `event payload exposes no aggregate`(MEDIUM)
**persistence-rules (C-6)** — `jpa entity does not leak outside persistence`(HIGH)
**shared-kernel-rules (C-7)** — `shared-kernel has no reverse dependency`(HIGH)

> C-3·C-4·C-7은 서드파티 동명패키지(예: `org.springframework.data.domain`) 오탐을 `archrules-common`의 `AppPackages.sameApp`(앱-베이스 가드)로 차단한다.

---

## 빌드 · 테스트 · 배포

```bash
./gradlew build                                   # 전 모듈 빌드 + self-test
./gradlew publishToMavenLocal -ParchrulesVersion=0.2.0   # 로컬 발행
```
JitPack은 git **태그**를 요청 시 빌드한다 — 태그를 밀면 `com.github.ryu-qqq.archrules-platform:<module>:<tag>` 로 소비 가능(`jitpack.yml`이 `publishToMavenLocal`로 빌드).

---

## 로드맵

- **소비 정합(SSOT 수렴)**: connectly-services의 인라인 ArchUnit 테스트 + `platform-archrules` 중복을 archrules-platform으로 일원화.
- **공통 SDK 번들**: spring-platform-commons에 규칙을 번들로 묶어 자동 적용 + 그쪽 JitPack 게시.
- **규칙 확장**: base 클래스 설정형 규칙, 멀티모듈 `--classes` 지원, org 존재 린트(모든 소비 레포가 워크플로우 참조하는지 감사).

---

## 설계 메모

- **Nebula 비채택(엔진만 흡수)**: `nebula-archrules-plugin`의 코어(SPI+ServiceLoader+self-test 헬퍼+report-only 게이트)는 흡수하되, Gradle 플러그인·variant·work-action 격리는 우리 규모에 불필요해 걷어냈다. 규칙은 우리 SPI에만 의존해 항상 이식 가능.
- **어노테이션-프리 상대매처**: jMolecules(어노테이션 필요)·Spring Modulith(Spring 결합)를 비채택하고 순수 패키지 매처로 자작 — 어떤 Spring+Java 헥사고날 프로젝트든 드롭인. 근거·PoC는 ADR-0001 참조.
- **외부 빌드 의존 0**: ArchUnit·JUnit 외 서드파티 없음.
