# 정본 지도 (Source-of-Truth Topology) — 기술부채 통합 설계

- **날짜**: 2026-06-18
- **상태**: 확정 (사용자 승인)
- **범위**: archrules-platform · spring-platform-commons · platform-commons-connectly · connectly-services · platform-bootstrap · platform-gitops 에 걸친 통합 작업의 **0번 기준 문서**
- **이 문서의 위치**: archrules-platform(메커니즘 정본 레포)의 docs. 이후 sub-project들이 이 지도를 기준으로 진행한다.

---

## 1. 배경 — 왜 이 문서가 필요한가

흩어지고 중구난방인 서비스·공통 코드·아키텍처 규칙·인프라를 한 번에 정리해 기술부채를 잡으려는데,
"무엇부터 손대야 의존이 안 꼬이나"가 막혀 있었다. 원인은 기술 난이도가 아니라 **같은 추상화가 여러 레포에 중복**되어
정본(SSOT)이 안 정해진 것이다.

특히 **ArchUnit 규칙이 서로 다른 3가지 접근으로 4곳에 존재**한다.

| 위치 | 정체 | 특징 |
|------|------|------|
| `archrules-platform` (이 레포) | SPI 신설계 | `ArchRulesService`+ServiceLoader+CLI 게이트. 외부의존 0. 규칙 5개. 현재 활발히 작업 중 |
| `spring-platform-commons/platform-archrules` | 정적 클래스 방식 | `HexagonalArchRules`/`DomainConventionRules`/`PersistenceConventionRules` + **Frozen baseline** + `DomainHealthReporter` (com.ryuqqq.platform) |
| `platform-commons-connectly/platform-archrules` | 위와 **동일 코드**(패키지명만 com.connectly.platform) | ryuqqq→connectly 승격본. 사실상 위와 1개 |
| `connectly-services` 인라인 | 실전 축적 자산 | crawling 한 서비스에만 수십 개 ArchTest(Dto·Listener·Assembler·TxManager·Factory·Layer·Flyway·HikariCP·DomainPurity·vo/aggregate/exception…). 하드게이트로 가동 중 |

요약:
- **메커니즘은 2갈래** — SPI 신설계(archrules-platform) vs 정적+Frozen+Reporter(commons, 2곳이지만 같은 코드).
- **규칙 자산의 보고는 connectly-services 인라인** — 가장 많고 실전 검증됨. 진짜 가치는 여기 있다.

공통 SDK도 2단계 파이프라인(개인 인큐베이터 → 회사 정본)으로 승격이 진행 중(6/12 모듈)이며,
archrules가 그 안에도 모듈로 들어가 있어 경계가 흐려져 있었다.

---

## 2. 결정 (Decisions)

1. **archrules 메커니즘 정본 = `archrules-platform` (SPI/ServiceLoader/CLI 방식)로 수렴한다.**
   - 근거: 가장 깔끔하고 이식성이 높다(외부의존 0). Nebula ArchRules 스타일의 의도된 차세대 설계이며 현재 활발히 작업 중인 후보다.
   - commons의 정적 방식은 폐기 대상. 단 **Frozen baseline 기능은 흡수**한다(아래 3-#1).

2. **공통 SDK는 기존 2단계 파이프라인을 유지한다.**
   - `spring-platform-commons`(com.ryuqqq.platform, JitPack) = 개인 인큐베이터.
   - `platform-commons-connectly`(com.connectly.platform, GitLab private) = 회사 정본(승격 대상).
   - 단 `platform-archrules` 모듈은 양쪽에서 폐기하고 `archrules-platform`로 대체한다.

3. **archrules-platform 배포 좌표는 독립 JitPack 유지(기본값).**
   - `com.github.ryu-qqq.archrules-platform:<module>:<tag>`.
   - connectly 정본 SDK 모듈로 흡수하지 않고 독립 레포로 둔다. (외부의존 0 원칙·이식성 보존. 재검토 여지는 있으나 현 단계 기본값.)

4. **connectly-services가 흩어진 서비스의 통합 종착지(모노레포)이며, archrules의 첫 실소비자다.**

5. **(2026-06-19 갱신) 규칙 라이브러리는 보편/특화를 모듈로 분리하지 않는다.**
   - 당초 #2 첫 슬라이스에서 connectly 특화 규칙을 `connectly-domain-rules` 별도 모듈로 분리했으나, 사용자 결정으로 번복. 특화 규칙도 `domain-rules` 한 모듈에 둔다(보편/특화 구분은 코드 레벨에서만 유지).
   - 함의: archrules-platform을 **사실상 connectly 전용 플랫폼**으로 본다. "이식 가능"은 상대 매처(root 무관) 수준에서만 유지. 상세: [[2026-06-19-domain-rules-categories-design]].

---

## 3. 정본 지도 (Target Topology)

```
┌─ 공통 SDK (2단계 파이프라인) ──────────────────────────────────┐
│  spring-platform-commons    = 개인 인큐베이터 (com.ryuqqq.platform, JitPack)
│         │ 다듬어서 승격
│  platform-commons-connectly = 회사 정본 (com.connectly.platform, GitLab private)
│         └ platform-archrules 모듈 폐기 → archrules-platform로 대체
└──────────────────────────────────────────────────────────────┘

┌─ ArchRules (SPI 수렴) ────────────────────────────────────────┐
│  archrules-platform = 메커니즘 정본 (SPI/ServiceLoader/CLI, 외부의존0)
│    ├ 흡수: commons의 Frozen baseline (브라운필드 필수)
│    ├ 규칙 라이브러리: hexagonal-rules · domain-rules · (+persistence-rules 신규)
│    └ 자산 공급원: connectly-services 인라인 수십 룰을 여기로 추출
│  좌표: com.github.ryu-qqq.archrules-platform (JitPack 독립)
└──────────────────────────────────────────────────────────────┘

┌─ 소비 모노레포 ───────────────────────────────────────────────┐
│  connectly-services (platform/ + services/{crawling, build…})
│    ├ 의존: platform-commons-connectly(공통 SDK) + archrules-platform(규칙)
│    └ 인라인 ArchTest 수십 개 → 규칙 추출 후 제거, reusable 워크플로우로 게이트
└──────────────────────────────────────────────────────────────┘

┌─ 인프라 (코드와 분리) ────────────────────────────────────────┐
│  platform-bootstrap (terraform/network/observability/finops/security)
│  platform-gitops (atlantis/gitops 배포)
│  ※ spring-platform-commons 안의 "platform-bootstrap 모듈"(조립 라이브러리)과는 이름만 같고 별개
└──────────────────────────────────────────────────────────────┘
```

---

## 4. sub-project 분해 + 순서 (의존 역순으로 도출)

| # | sub-project | 산출물 | 왜 이 순서 |
|---|------------|--------|-----------|
| **0** | 정본 지도 문서화 | 이 문서 | 나머지의 기준점 (완료) |
| **1** | archrules-platform에 **Frozen baseline 흡수** | `FreezingArchRule` 지원 + CLI/리포트 연동 | connectly-services는 기존 위반이 많음 → baseline 없이는 적용 불가 |
| **2** | connectly 인라인 룰 **추출 → 규칙 라이브러리** | persistence-rules 등 신규 규칙 모듈 | 실전 자산을 정본으로 끌어올림 |
| **3** | connectly-services가 **archrules-platform 소비** (라이브 e2e) | reusable 워크플로우 적용, 인라인 룰 제거 | 1·2가 선행되어야 가능. "라이브 e2e"가 통합 작업과 같은 일로 자연 해소 |
| **4** | 흩어진 서비스 → connectly-services 통합 + 공통 SDK 의존 | 서비스 이식 | 규칙 게이트가 선 위에서 안전하게 |
| **5** | commons/platform-archrules 폐기 | 중복 제거 | 3·4 완료 후 |

**핵심 통찰**: 처음에 "지금은 불가"라고 보였던 라이브 e2e는 별도 작업이 아니라 #3에서 통합 작업과 같은 일로 풀린다.
throwaway 소비 레포를 따로 만들 필요가 없고 connectly-services가 첫 실소비자가 된다.

---

## 5. 다음 단계

이 문서는 sub-project #0의 산출물이다. 다음은 **sub-project #1 (Frozen baseline 흡수)** 의 구현 계획 수립이다.
#1은 archrules-platform 단독 작업이라 범위가 명확하고, 이후 단계의 전제(브라운필드 대응)를 깐다.

> 각 sub-project는 자체 spec → plan → 구현 사이클을 갖는다. 이 문서는 그 위의 기준 지도다.
