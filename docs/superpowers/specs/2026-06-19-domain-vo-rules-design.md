# 도메인 VO 규칙 추출 (sub-project #2 — 첫 슬라이스) 설계

- **날짜**: 2026-06-19
- **상태**: 확정 (사용자 승인)
- **상위**: [[2026-06-18-source-of-truth-topology-design]] 의 sub-project #2(connectly 인라인 룰 → 규칙 라이브러리 추출)의 첫 수직 슬라이스.
- **범위**: connectly-services `crawling-domain`의 `VOArchTest`(9규칙)를 archrules-platform 규칙 라이브러리로 변환. 이 슬라이스가 변환·이식·self-test 픽스처 패턴을 확립해 이후 카테고리(aggregate/exception/criteria/package/purity)는 반복한다.

---

## 1. 배경

connectly-services는 도메인 architecture 테스트를 인라인 JUnit(`@Test` + `importPackages(BASE_PACKAGE)`)으로 수십 개 보유한다. 이 자산을 archrules-platform의 SPI(`ArchRulesService` + ServiceLoader + CLI) 방식으로 끌어올려 폴리레포가 공유·게이트하게 한다. 첫 슬라이스는 `VOArchTest`(9규칙).

연구 결과 9규칙은 두 성격으로 갈린다:
- **보편**: 어느 헥사고날/DDD 도메인에나 맞음.
- **connectly 특화**: connectly `platform-common-domain` 컨벤션(ID VO의 forNew/isNew, Enum의 displayName)에 묶임.

→ 정본 지도의 "이식 가능" 원칙을 지키기 위해 **두 층으로 분리**한다.

---

## 2. 결정

1. **두 층 분리**: 보편 규칙은 `domain-rules`, connectly 특화는 신규 모듈 `connectly-domain-rules`(둘 다 archrules-platform 안, 같은 SPI).
2. **상대 매처 유지**: connectly가 이미 `..vo..`를 쓰므로 root 하드코딩 없음. `BASE_PACKAGE`/`importPackages`만 제거(import는 runtime 담당). archrules-platform 이식성 원칙과 합치.
3. **archrules 컨벤션 강제 부여**: 각 규칙에 `.as(name)` + `.because(reason)` + priority + `allowEmptyShould(true)`. 커스텀 `ArchCondition`은 규칙 클래스에 `private static`으로 이식.
4. **어노테이션 금지 3규칙(lombok/jpa/spring) 제외** (조사 후 결정):
   - **Lombok**: 어노테이션이 `RetentionPolicy.SOURCE`라 컴파일된 `.class`에 남지 않음 → ArchUnit(바이트코드 분석)으로 검출 불가. `notBeAnnotatedWith("lombok.*")`는 항상 통과하는 **no-op**.
   - **JPA/Spring**: hexagonal-rules의 `domain is framework-free`(의존 자체 차단)가 상위호환이고, connectly는 gradle `forbiddenDependencies`로 빌드 레벨에서 이미 차단. 또한 violation 픽스처에 해당 어노테이션 의존이 필요해 **"외부 빌드 의존 0"** 원칙과 충돌.
   - 결과: 남는 규칙은 전부 외부 의존 없이 진짜 violation 픽스처로 self-test 가능.

---

## 3. 규칙 분류 (어노테이션 금지 제외 후 6규칙)

| # | 규칙 | 분류 | 모듈 | priority | 규칙 이름(.as) |
|---|------|------|------|----------|----------------|
| 1 | VO는 record (Enum/Interface/Abstract 제외) | 보편 | domain-rules | HIGH | `domain VO is record` |
| 2 | VO는 정적팩토리 `of()` | 보편 | domain-rules | HIGH | `domain VO has static factory of` |
| 8 | `create*()` 메서드 금지 | 보편 | domain-rules | MEDIUM | `domain VO has no create method` |
| 3 | ID VO는 `forNew()` | 특화 | connectly-domain-rules | HIGH | `connectly id VO has forNew` |
| 4 | Long ID VO는 `isNew()` (UUID 제외) | 특화 | connectly-domain-rules | MEDIUM | `connectly long id VO has isNew` |
| 9 | Enum VO는 `displayName()` | 특화 | connectly-domain-rules | MEDIUM | `connectly enum VO has displayName` |

보편 3 + 특화 3. (원본 9규칙 중 5·6·7 어노테이션 금지는 결정 4에 따라 제외.)

**패키지 매처**: connectly 원본 충실하게 `..vo..`. (VO는 도메인에만 존재한다는 전제. 다른 레이어 vo 충돌은 현 슬라이스 범위 밖.)

**커스텀 ArchCondition 이식 목록**:
- 보편: `beRecords()`(1), `haveStaticMethodWithName("of")`(2), `notHaveMethodsWithNameStartingWith("create")`(8).
- 특화: `haveStaticMethodWithName("forNew")`(3), `haveLongFieldAndIsNewMethod()`(4), `haveMethodWithName("displayName")`(9).

규칙 1·2·8의 `that()` 필터(Enum/Interface/Abstract/Fixture/Mother/Test/Anonymous/Member 제외)는 원본 그대로 유지한다.

---

## 4. 모듈 구조

### domain-rules (기존 모듈에 추가)
- 신규 `VoRules`(일반 클래스): VO 보편 6규칙의 `ArchRule` 상수 + 커스텀 `ArchCondition` private static 헬퍼 보유. (카테고리가 6개로 늘 것이므로 카테고리별 클래스로 분리.)
- `DomainRules`(기존 `ArchRulesService`): `getRules()`에 기존 2규칙 + `VoRules`의 6규칙을 합쳐 반환.
- self-test: `fixture/compliant/domain/vo/`, `fixture/violation/domain/vo/` 픽스처 + 기존 `DomainRulesTest`/`RuleConventionTest`가 .as/priority/because 강제.

### connectly-domain-rules (신규 모듈)
- `settings.gradle`에 모듈 등록. `build.gradle`: `api project(':archrules-api')` + test에 runtime·junit·archunit (domain-rules와 동일 패턴).
- `ConnectlyDomainRules implements ArchRulesService`: VO 특화 3규칙.
- `src/main/resources/META-INF/services/com.ryuqqq.archrules.api.ArchRulesService`에 FQN 등록.
- self-test: compliant/violation 픽스처 + RuleConventionTest 동등물.

---

## 5. 검증 / 완료 기준

- `./gradlew build` 통과(전 모듈 self-test 포함).
- domain-rules: VO 보편 3규칙(record/of/create금지)이 violation 픽스처를 각각 잡고 compliant 픽스처는 통과.
- connectly-domain-rules: 특화 3규칙이 violation 픽스처를 잡고 compliant는 통과.
- 두 모듈의 RuleConventionTest가 모든 노출 규칙의 .as/priority/because 컨벤션을 강제.

**범위 밖(후속 슬라이스)**: aggregate/exception/criteria/package/purity 카테고리, connectly-services 인라인 룰 제거·소비(#3).
