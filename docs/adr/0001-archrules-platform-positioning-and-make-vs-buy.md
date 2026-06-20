# 0001. archrules-platform 포지셔닝: Nebula식 엔진 유지 + 헥사고날 규칙 자작/위임 make-vs-buy

- 상태: Accepted
- 날짜: 2026-06-20

## 맥락 (Context)

`archrules-platform`은 폴리레포 환경에서 공통으로 쓰는 ArchUnit 규칙 플랫폼이다.
`project.yaml`상 포지셔닝은 **"Nebula 스타일 경량 자체구현, 외부 빌드 의존 0"**이다.
현재 구성:

- **SPI**: `ArchRulesService`
- **runtime**: Runner / CLI / freeze (점진적 강제 메커니즘)
- **규칙 라이브러리**: domain-rules, hexagonal-rules, context-isolation-rules(C-3),
  shared-kernel-rules(C-7), persistence-rules(C-6), messaging-rules(C-5),
  security-boundary-rules(C-4 일부)

규칙 작성 컨벤션은 고정되어 있다:
`public static final ArchRule` + `.as()` + `.because()` + `.allowEmptyShould(true)`,
ServiceLoader 등록, 그리고 **어노테이션-프리 상대 패키지 매처**(`..domain..` 등).

### 트리거

이 ADR은 다음 질문에서 출발한 조사의 산물이다:

> "우리가 만드는 게 Netflix식 archrules와 같은가? jMolecules / Spring Modulith를
> 써야 하는가? 구조를 바꿔야 하는가?"

즉 (1) 우리 플랫폼의 정체성을 외부 선례에 비추어 확인하고, (2) 헥사고날/DDD 규칙을
자작할지(make) 검증된 표준에 위임할지(buy)를 결정해야 한다.

### 승계하는 선례

이 ADR은 새로운 발명이 아니라 **이미 실증된 판단을 승계·정식화**한다.

- **spring-platform-commons가 동일 핵심 판단을 먼저 실증함**
  - vault `wiki/projects/spring-platform-commons/adoption-gap.md`:
    "Nebula 출하 규칙은 API 위생 중심이고, 레이어 경계/헥사고날 규칙은 미제공 →
    자작 영역"이라고 진단.
  - `build-out-backlog.md`: P0 항목 `platform-archrules`가
    `DOMAIN_FRAMEWORK_FREE` / `APPLICATION_NO_WEB_OR_PERSISTENCE` / `HEXAGONAL_LAYERS`
    3종을 **상대 매처로 이미 구현(2026-06-07)**.
  - 결론: "엔진 = Nebula식 + 헥사고날 = 자작 + 상대 매처" 조합은 이미 채택·검증됨.
    본 ADR은 이를 archrules-platform 레포 맥락으로 승계한다.

- **선행 ADR-0002 (spring-platform-commons, repo 토폴로지)**
  - monorepo vs 멀티레포를 "무후회(no-regret) 단계"로 defer하고, 1단계는
    **"platform-commons + platform-archrules 한 레포"**로 시작하기로 결정.
  - archrules-platform이 지금 **별도 레포로 분기한 현 상태**는 이 분기를 건드린다.
    → Consequences에서 정합화 메모로 다룬다(이 ADR에서 완전 결정하지는 않음).

## 결정 동인 (Decision Drivers)

- **범용성(드롭인)**: 임의의 Spring + Java 헥사고날 프로젝트(connectly + 개인)에
  곧바로 끼워 넣을 수 있어야 한다.
- **어노테이션 비결합**: 소비 레포에 stereotype 어노테이션을 강제하지 않는 편을
  선호한다(드롭인 마찰 최소화).
- **다중레포 공유 + 점진 강제**: freeze / baseline 기반의 점진적 강제(Nebula식)가
  필요하다.
- **make vs buy 균형**: 검증된 표준 재사용의 이득 vs 직접 유지비/false-positive
  리스크 사이의 균형.

## 검토한 옵션 (Considered Options)

조사 결과 핵심 외부 선례 3종의 성격을 먼저 정리한다(근거는 More Information의 출처 참조).

- **Netflix Nebula ArchRules** = 아키텍처에 무관한 *다중레포 룰 배포 엔진*
  (ArchUnit 직접 사용, `getRules(): Map<String, ArchRule>`). 출하 규칙 라이브러리
  (guava / joda / javax / nullability / security / spring / testing-frameworks /
  deprecation / gradle-plugin-development)는 **전부 라이브러리 위생·마이그레이션·
  베스트프랙티스**이며, **아키텍처(헥사고날/DDD/레이어) 규칙은 0건**.
  `archrules-common` 헬퍼층(커스텀 DescribedPredicate / ChainableFunction /
  ArchCondition) 존재. 룰 스타일은 어노테이션-프리 상대/의존 매처(우리와 동일).
- **jMolecules** `JMoleculesArchitectureRules.ensureHexagonal/Layering/Onion(STRICT)`
  = C-2 동등물이지만 **stereotype 어노테이션 / 타입 계층을 요구**한다
  (`StereotypeLookup`도 어노테이션 *위치*만 바꿀 뿐 어노테이션-프리는 아님).
  DDD 빌딩블록(C-1 영역)도 어노테이션 전용.
- **Spring Modulith** `ApplicationModules.verify()` = no-cycles + API-패키지-only +
  allowedDependencies 화이트리스트 = **C-3와 거의 1:1**. `@NamedInterface` = 우리 `.api`.
  단 기본 모델은 평면 1단 모듈이라 우리 `services/<svc>/<context>` **2단 중첩과 직접
  대응하지 않음**. `ApplicationModuleDetectionStrategy.getModuleBasePackages` SPI +
  `detectNamedInterfaces`(1.4+, recursive "api" 매칭)로 **어노테이션-프리 2단 중첩
  우회가 가능할 수 있으나 미실증**(이슈 #182 사이클 탐지 부작용 = sharp edge).
  1.3+는 classpath에 jMolecules가 있으면 헥사고날 검증을 자동 트리거.

### 축 A — 엔진

| 옵션 | 설명 | 트레이드오프 |
|---|---|---|
| **A1 [권장·기실증]** | Nebula식 자작 엔진 유지(SPI + runtime + freeze) | Netflix fleet 검증 패턴과 일치, 외부 빌드 의존 0. 엔진 자체 유지비 발생 |
| A2 | 순수 ArchUnit만 사용 | 공유/배포/freeze 메커니즘 부재 → 다중레포 요구 불충족 |
| A3 | 외부 도구 채택 | 범용성·통제권 상실, "외부 의존 0" 포지셔닝과 충돌 |

### 축 B — 룰 작성 스타일

| 옵션 | 설명 | 트레이드오프 |
|---|---|---|
| **B1 [권장]** | 어노테이션-프리 상대 매처 유지 | 드롭인 마찰 최소, 소비 레포 비침투. 패키지 규약 의존 → false-positive 리스크 |
| B2 | jMolecules 어노테이션 / stereotype 채택 | 의미가 명시적·표준적. 소비 레포에 어노테이션 강제(드롭인 마찰↑) |
| B3 | 하이브리드(상대 매처 기본 + 선택적 어노테이션 인식) | 유연하나 두 경로 유지 비용·혼란 |

### 축 C — 헥사고날 룰 make-vs-buy (핵심)

| 옵션 | 설명 | 트레이드오프 |
|---|---|---|
| C-buy | jMolecules / Modulith 전면 채택 | 표준 재사용·유지비↓. 어노테이션 강제(B와 충돌), 2단 중첩 미대응 |
| C-make | 헥사고날 룰 전부 자작 유지 | 완전 통제·어노테이션-프리. C-2/C-3 표준과 중복 유지비, false-positive 책임 |
| **C-hybrid [권장]** | 엔진 + C-1/C-5/C-6/C-7은 자작 유지, C-2/C-3는 jMolecules/Modulith 위임 검토(특히 C-3 ↔ Modulith), **단 2단 중첩 PoC 게이트 통과 조건** | 검증 표준 재사용 + 어노테이션-프리 유지 가능성. PoC 미통과 시 자작 폴백 필요(불확실성) |

### 축 D — archrules-common 프리미티브 모듈 도입

| 옵션 | 설명 | 트레이드오프 |
|---|---|---|
| **D1 [권장, Netflix식]** | 공통 프리미티브(DescribedPredicate / ArchCondition / 매처 헬퍼) 모듈 추출 | Netflix `archrules-common`과 동형, 규칙 모듈 간 중복 제거 |
| D2 | 미도입 | 규칙 모듈마다 헬퍼 중복 → 드리프트·유지비↑ |

### 축 E — org 정책 분리

| 옵션 | 설명 | 트레이드오프 |
|---|---|---|
| **E1 [권장]** | authhub 등 특화 규칙은 소비 레포(connectly-services)가 엔진을 상속해 자체 정의 | 플랫폼은 범용 유지, org 정책은 소비처 책임. 소비 레포에 약간의 저작 부담 |
| E2 | 본 플랫폼에 org 특화 규칙 포함 | 플랫폼 범용성 훼손, 개인 프로젝트 드롭인 시 불필요 규칙 혼입 |

## 결정 (Decision Outcome)

**채택: A1 + B1 + C(자작 유지 + C-3↔Modulith PoC 게이트) + D1 + E1.**

축별 확정:

- **축 A — 엔진: A1 채택.** Netflix Nebula ArchRules 패턴(`ArchRulesService` SPI +
  ServiceLoader + library/runner 분리 + aggregate 리포트 + 관심사별 1 라이브러리)을
  **자작·유지**한다. 외부 빌드 의존 0을 유지한다.
- **축 B — 룰 스타일: B1 채택.** 어노테이션-프리 + 상대 패키지/의존 매처(`..domain..` 등)를
  유지한다. jMolecules의 어노테이션 / stereotype 방식은 **비채택**한다(드롭인 마찰 회피).
- **축 C — 헥사고날 make-vs-buy: C-1 ~ C-7 전부 자작 유지.** 단 **C-3(컨텍스트 격리 + `.api`)
  하나만 Spring Modulith로의 대체 여부를 PoC 게이트로 평가**한다.
  - **PoC 통과 조건**: 우리 `services/<svc>/<context>` **2단 중첩** 구조에서
    커스텀 `ApplicationModuleDetectionStrategy`(`getModuleBasePackages`) +
    `detectNamedInterfaces`(Modulith 1.4+) 기반 **어노테이션-프리 우회**가
    **이슈 #182(사이클 탐지 부작용) 없이 동작**할 것.
  - **통과 시에만** C-3를 Spring Modulith로 대체한다. **실패·부적합 시** 현 자작
    모듈(`context-isolation-rules`)을 그대로 **유지**한다.
  - 따라서 **유일한 buy 후보는 C-3 ↔ Modulith 단 하나**이며, 그조차 PoC 게이트
    통과를 전제로 한다.
  - **✅ PoC 결과 (2026-06-20, 게이트 종결): C-3 자작 유지(MAKE)로 확정.**
    `poc/modulith-c3/`에서 실증 — 커스텀 `ApplicationModuleDetectionStrategy`(2단 중첩을
    평면 모듈로) + `NamedInterfaces.builder().matching("api")`로 **어노테이션-프리 C-3 강제가
    기술적으로 VIABLE**(성공기준 4/4 PASS, #182 사이클 부작용 **없음**). 그러나
    `spring-modulith-core:2.1.0`이 **spring-boot:4.1.0 런타임 의존을 강제** → archrules-platform의
    "Spring-free·어디나 드롭인"(A1) 가치와 정면 충돌. 자작 `context-isolation-rules`는 순수
    ArchUnit·버전 독립·커스텀 메시지로 이미 동작하므로 **대체 실익 없음**. → **C-3는 자작 유지**,
    Modulith는 "Spring Boot 4.x 프로젝트의 보조 검증 레이어"로만 후보로 남긴다.
- **B1의 직접 귀결 — C-2 jMolecules 위임 배제:** B1(어노테이션-프리)을 택했으므로
  어노테이션을 강제하는 jMolecules `ensureHexagonal/Layering/Onion`에 **C-2(헥사고날
  레이어링)를 위임할 수 없다**. 따라서 **C-2는 자작(`hexagonal-rules`)으로 유지**한다.
  (조사 단계의 C-hybrid가 열어 두었던 "C-2를 jMolecules에 위임" 선택지는 본 결정에서 닫힌다.)
- **축 D — archrules-common: D1 채택.** Netflix식 공용 프리미티브 모듈을 도입한다.
  반복 발견된 **base-package 가드(서드파티 false-positive 차단)**, `ContextKeys` 등
  커스텀 `DescribedPredicate` / `ArchCondition`을 **공용 헬퍼층으로 추출**한다.
- **축 E — org 정책 분리: E1 채택.** authhub 격리 등 프로젝트 특화 규칙은
  archrules-platform이 아니라 **소비 레포(connectly-services)가 `ArchRulesService`를
  상속/구현해 자체 정의**한다(Netflix "사내 내부 라이브러리" 모델). 플랫폼은 범용을 유지한다.

요컨대 **엔진·룰 스타일·헥사고날/DDD 규칙은 전부 자작을 확정**하고, buy 가능성은
**C-3 ↔ Spring Modulith 한 지점**으로 좁힌 뒤 그마저 **PoC 게이트로 조건부화**한다.

## 결과 (Consequences)

### 긍정 (positive)

- 엔진(A1)과 룰 스타일(B1)이 **Netflix fleet 검증 패턴과 일치** — 다중레포 룰 배포의
  실증된 길을 따른다.
- 어노테이션-프리 드롭인으로 소비 레포 침투 최소 → connectly + 개인 프로젝트 양쪽에
  적용 가능.
- freeze / baseline 기반 점진적 강제 유지.
- archrules-common(D1) 추출로 규칙 모듈 간 헬퍼 중복 제거, Netflix식 구조에 수렴.

### 부정 / 리스크 (negative / risks)

1. **false-positive 리스크**: 헥사고날을 순수 패키지 매처로 잡는 접근은 본질적으로
   false-positive에 취약하다. 이미 gemini / opus 리뷰에서 **2회 발견**되었고
   base-package 가드로 대응 중. 어노테이션-프리(B1)를 유지하는 한 상존하는 비용.
2. ~~**C-3 ↔ Modulith 2단 중첩 우회 미실증**~~ **→ 해소(2026-06-20 PoC)**: 가설은
   **VIABLE로 실증**됨(어노테이션-프리 2단 중첩 동작, #182 부작용 없음). 그러나 Modulith가
   **spring-boot 4.x 런타임 의존을 강제**하는 비용이 드러나, buy가 아니라 **자작 유지(MAKE)로
   확정**. 리스크가 "미실증 불확실성"에서 "Spring 결합 비용"으로 바뀌어 해소됨. (poc/modulith-c3/)
3. **C-5/C-6/C-7 표준 미제공은 부정적 증거 기반**: Nebula/jMolecules/Modulith가 이
   영역 규칙을 제공하지 *않는다*는 사실에 근거한 자작 정당화 — "없음"의 입증은
   상대적으로 약한 근거이므로 향후 표준 등장 시 재검토 여지.
4. **레포 분기 정합 필요**: archrules-platform이 별도 레포로 분기한 것은
   spring-platform-commons **ADR-0002의 "1단계 = 한 레포" 분기와 정합화가 필요**하다.
   본 ADR은 이를 결정하지 않고 후속 토폴로지 결정으로 넘긴다.
5. **설계 "왜"의 1차 출처 미확보(조사 공백)**: Drotbohm(Modulith/jMolecules 저자)의
   설계 의도 1차 출처를 확보하지 못했다 — 위임 판단의 근거 일부가 2차 자료에 의존.
6. **B1 귀결 — C-2 jMolecules 위임 배제 확정**: B1(어노테이션-프리)을 택함으로써
   jMolecules `ensureHexagonal/Layering/Onion`(어노테이션 강제)에 C-2를 위임하는
   경로가 닫혔다. C-2(헥사고날 레이어링)는 자작(`hexagonal-rules`)으로 유지되며,
   그에 따라 1번의 false-positive 비용을 C-2도 계속 부담한다.

### Follow-up (우선순위)

1. **archrules-common 프리미티브 모듈 추출**(D1 구현) — **즉시**. base-package 가드 /
   `ContextKeys` 등 커스텀 `DescribedPredicate` / `ArchCondition`을 공용 헬퍼층으로 추출.
2. ~~**C-3 ↔ Modulith 2단 중첩 PoC** — **게이트**~~ **✅ 완료(2026-06-20)**: VIABLE이나
   spring-boot 4.x 결합 비용으로 **자작 유지 확정**. 게이트 종결. 산출물 `poc/modulith-c3/`
   (+ `FINDINGS.md`). Modulith는 Spring Boot 4.x 프로젝트의 보조 검증 레이어로만 후보 잔존.
3. **connectly-services가 엔진을 상속해 authhub 등 org 정책을 자체 정의**(E1 구현) —
   connectly 실코드가 생길 때.
4. **archrules-platform 레포 토폴로지를 spring-platform-commons ADR-0002(repo 토폴로지)와
   정합화** — 별도 레포화가 ADR-0002의 "1단계 = 한 레포" 분기와 충돌하지 않도록 후속 결정.

## 추가 정보 (More Information)

### 출처

- Netflix Nebula ArchRules
  - github.com/nebula-plugins/nebula-archrules
  - github.com/nebula-plugins/nebula-archrules-plugin
  - netflixtechblog "Scaling ArchUnit with Nebula ArchRules"
- jMolecules
  - github.com/xmolecules/jmolecules-integrations — jmolecules-archunit 소스
- Spring Modulith
  - docs.spring.io/spring-modulith/reference/verification.html
  - docs.spring.io/spring-modulith/reference/fundamentals.html
  - Spring Modulith 1.3 릴리스 블로그
  - 이슈 #578, #1009, #182

### 관련 문서 (vault)

- `wiki/projects/spring-platform-commons/adoption-gap.md` — Nebula gap 진단
- `wiki/projects/spring-platform-commons/build-out-backlog.md` — P0 platform-archrules
- spring-platform-commons **ADR-0002** — repo 토폴로지(monorepo vs 멀티레포 defer)
- vault raw: "Scaling ArchUnit with Nebula ArchRules"
