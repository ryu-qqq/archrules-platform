# 도메인 규칙 카테고리 추출 + 모듈 병합 설계 (sub-project #2 나머지)

- **날짜**: 2026-06-19
- **상태**: 확정 (사용자 승인)
- **상위**: [[2026-06-18-source-of-truth-topology-design]] sub-project #2. [[2026-06-19-domain-vo-rules-design]](VO 첫 슬라이스)의 후속.
- **범위**: connectly `crawling-domain`의 나머지 architecture 카테고리(aggregate/exception/criteria/package/purity)를 archrules-platform으로 추출 + `connectly-domain-rules` 모듈을 `domain-rules`로 병합.

---

## 1. 방향 전환 — 두 층 분리 번복

[[2026-06-19-domain-vo-rules-design]]에서 connectly 특화 규칙을 별도 모듈(`connectly-domain-rules`)로 분리했으나, **사용자 결정으로 이를 번복**한다. 특화 규칙도 `domain-rules` 한 모듈에 둔다.

함의: archrules-platform을 **사실상 connectly 전용 플랫폼**으로 본다. 정본 지도의 "이식 가능" 원칙은 상대 매처(`..vo..` 등 root 무관) 수준에서만 유지하고, "보편/특화 모듈 분리"는 포기한다. 정본 지도 문서(#2 항목)도 이에 맞게 갱신한다.

보편/특화 **구분 자체는 코드 레벨(클래스/규칙 이름)에서 유지**하되 모듈은 하나다 — 나중에 다시 분리할 여지를 남긴다.

---

## 2. 조사 결과 (원본 73규칙)

| 카테고리 | 원본 규칙 | 큐레이션 후(추정) |
|---|---|---|
| aggregate | 22 | ~14 (보편 10 + 특화 4) |
| exception | 20 | ~12 |
| criteria | 10 | ~6 |
| package | 7 | ~5 |
| purity | 14 | framework-free 확장으로 흡수 |

**제외 대상(엄격 큐레이션 — 사용자 결정)**:
- **어노테이션 금지(lombok/jpa/spring)**: lombok은 SOURCE retention이라 ArchUnit 검출 불가(no-op). jpa/spring은 hexagonal-rules `domain is framework-free`가 의존 전체를 더 포괄적으로 차단(어노테이션만 보는 원본보다 강함) + violation 픽스처에 외부 의존 필요. 모든 카테고리에서 제외.
- **soft/문서용 규칙**: aggregate #15(비즈니스 메서드 동사, 경고-only), exception #20(ArchRule 아님, println 경고), criteria #9(문서 목적, 항상 통과). 추출 실익 0 → 제외.
- **fixture 검사 규칙**: aggregate #19~22(`*Fixture` 클래스에 forNew/of/reconstitute 강제). 테스트 픽스처 메타 규칙이라 라이브러리 추출 범위 밖 → 제외.
- **purity 중복**: jpa/spring/jackson/hibernate/validation 금지는 framework-free와 중복 → 제외.

**루트 하드코딩 → 상대 매처 전환**: criteria #8, purity의 레이어 의존 규칙 등 `com.connectly.crawling.domain` 하드코딩을 `..domain..` 상대 매처로 전환(이식성).

---

## 3. 카테고리별 추출 대상 (큐레이션 결과)

### aggregate (AggregateRules)
보편: setter 금지 · 생성자 private · id 필드 final · 외래키는 VO(원시타입 금지) · `..aggregate..` 패키지 위치 · public · final 금지 · 외부 레이어(application/adapter) 의존 금지 · createdAt(Instant·final) · updatedAt(Instant·not final).
특화: `forNew()` · `of()` · `reconstitute()` 정적 팩토리 · Instant 필드 최소 1개.

### exception (ExceptionRules)
보편: ErrorCode enum의 `getCode()`/`getHttpStatus()`/`getMessage()` 존재 · `getHttpStatus()` 반환타입이 Spring 타입 아님 · ErrorCode enum public · `..exception..` 패키지 위치(ErrorCode·Concrete) · Concrete는 RuntimeException 상속 · 외부 레이어 의존 금지 · 접근 제한(domain+adapter).
특화: ErrorCode 인터페이스 구현 · DomainException 상속(이름 기반 — 픽스처에 stub 정의로 self-test 가능).
제외: DomainException 위치/상속(#16·17, `..domain.common.exception` 중간 세그먼트 하드코딩 — 이식성 모호).

### criteria (CriteriaRules)
보편: `*Criteria`는 `..query.criteria..` 위치 · criteria는 record · 이름 `*Criteria`로 끝남 · `of()` 정적 팩토리 · public · 외부 레이어 의존 금지(상대 매처로 전환).

### package (PackageRules)
보편: domain.common.util은 인터페이스만 · DomainEvent 구현체는 `..event..` 위치 · DomainException 서브타입은 `..exception..` 위치 · BC 간 순환 의존 금지.
제외: common 이름 규약·common 접근(절대 패키지 열거 — connectly 특화 하드코딩).

### purity → hexagonal-rules `DOMAIN_FRAMEWORK_FREE` 확장
기존 금지목록(spring/jakarta/hibernate/jackson/junit)에 **추가**: `org.apache.commons..` · `com.google.common..`(Guava) · `io.vavr..` · `com.google.gson..` · `org.slf4j..` · `ch.qos.logback..` · `org.apache.logging.log4j..` · `java.time.LocalDateTime`. (domain-rules가 아닌 hexagonal-rules 파일 수정 → 카테고리 작업과 병렬 가능.)

---

## 4. 모듈 구조

### 카테고리 규칙 클래스 패턴
각 카테고리는 자기 규칙 맵을 소유:
```java
final class AggregateRules {
    private AggregateRules() {}
    // static final ArchRule 상수 + private static ArchCondition 헬퍼
    static Map<String, ArchRuleSpec> rules() { /* 키→ArchRuleSpec(priority) */ }
}
```
`DomainRules`(단일 `ArchRulesService`)의 `getRules()`는 모든 카테고리 `rules()`를 **병합만** 한다(`Map.ofEntries` 또는 누적 put). 새 카테고리 추가 = 병합 한 줄. 기존 `VoRules`도 이 패턴(`rules()` 제공)으로 정렬한다.

### 병렬 격리
각 카테고리 = 완전 독립 파일집합: `XxxRules.java` + 픽스처(`fixture/{compliant,violation}/domain/xxx/`) + `XxxRulesTest.java`(자기 `rules()` 직접 `Runner.check` self-test). 서로 안 겹침 → 병렬 안전. 유일한 공유 파일 `DomainRules.java`(병합 지점)는 controller가 순차 통합.

---

## 5. 작업 순서

1. **Task 0 (단독, baseline)**: `connectly-domain-rules` 모듈 제거 → `domain-rules`로 흡수. 특화 VO 규칙 이동. `DomainRules.getRules()`를 카테고리 `rules()` 병합 구조로 리팩토링(기존 도메인 2규칙 + VO 보편 + VO 특화). `settings.gradle` 갱신. self-test 전부 통과 확인.
2. **병렬 (4 에이전트 동시)**: aggregate · exception · criteria · package. 각자 독립 파일집합 + 자기 `rules()`. `DomainRules` 합류 안 함.
3. **통합 (controller)**: 4개를 `DomainRules.getRules()`에 병합 + 전체 빌드.
4. **purity (병렬 가능, 별도 파일)**: hexagonal-rules `DOMAIN_FRAMEWORK_FREE` 금지목록 확장.

---

## 6. 검증 / 완료 기준

- `./gradlew build` 통과(전 모듈 self-test 포함).
- 각 카테고리: violation 픽스처를 규칙이 각각 잡고 compliant 픽스처는 통과.
- 모든 추출 규칙은 `.as(name)` + `.because(reason)` + priority + `allowEmptyShould(true)`. `RuleConventionTest`가 강제.
- 모든 규칙은 상대 매처만 사용(root 하드코딩 0). 외부 빌드 의존 0(ArchUnit·JUnit만).
- purity: 확장된 framework-free가 추가 라이브러리(Guava 등) 의존 픽스처를 잡음.

**범위 밖(후속)**: connectly-services 인라인 룰 제거·소비(#3), adapter/application 카테고리.
