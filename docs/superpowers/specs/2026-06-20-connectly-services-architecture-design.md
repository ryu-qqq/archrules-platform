# connectly-services 아키텍처 설계 (헥사고날 모듈러 모놀리식)

- 작성일: 2026-06-20
- 상태: Proposed (사용자 리뷰 대기)
- 목적: 흩어진 서비스(marketplace, crawlinghub, fileflow, authhub, gateway)를 하나의 레포 `connectly-services`로 합치되, **나중에 서비스별로 독립 추출·스케일아웃하기 쉽게** 만든다. 동시에 이 설계를 강제할 **archrules(ArchUnit) 규칙 세트**를 정립한다.
- 산출물 범위: **아키텍처 기준 + archrules 규칙 세트 + 공통 플랫폼 자산 통합 전략**. (기존 코드의 실제 물리적 이전은 별도 마이그레이션 계획에서 다룬다.)

---

## 0. 한 줄 요약

> **컨텍스트=모듈을 최소 단위로 전부 쪼개되, 모든 컨텍스트 경계는 동일한 seam(읽기=동기 포트 / 쓰기·전파=SNS→SQS 이벤트)으로만 소통한다. 인증은 gateway 엣지에서 서명된 내부 JWT로 주입하고 다운스트림은 재검증하지 않는다. 배포·DB 선은 bootstrap 조립으로 늦게 긋는다. 이 모든 규율을 archrules ArchUnit 규칙 세트로 CI에서 강제한다.**

---

## 1. 결정 로그 (Decision Log)

| # | 결정 | 근거 / 트레이드오프 |
|---|---|---|
| D1 | **service-first 모듈러 모놀리식.** 1순위 분할축 = 서비스(바운디드 컨텍스트), 그 아래 헥사곤 레이어 | 컨텍스트=모듈=컴파일 경계. 추출 = 모듈 들어내기. layer-first(현 Marketplace)는 합치면 컨텍스트 경계가 관습이라 무너짐 |
| D2 | **3계층 용어 고정: Context / Service / Bootstrap** | Context만 고정 단위. Service는 "현재 배포·DB·일관성을 공유하는 묶음"이라는 *뷰*이며 Bootstrap 조립으로 늦게·싸게 바뀜 |
| D3 | **컨텍스트는 product/order/claim처럼 잘게 쪼갠다(모듈). 단 서비스/DB/트랜잭션으로는 아직 쪼개지 않는다** | "BC ≠ 배포 단위." 모듈 분할은 싸고 되돌리기 쉬움. DB/트랜잭션 분할은 분산 사가 비용 → 부하가 정당화할 때만 |
| D4 | **Seam은 단 2종만 허용: S1 동기 읽기 포트 / S2 도메인 이벤트(브로커).** 컨텍스트 넘는 원자적 쓰기(구 S3) 금지 | 한 tx = 한 애그리거트 + outbox 행. 원자성이 진짜 필수면 → 그 둘은 사실 한 컨텍스트(합쳐라). 중간지대 제거로 추출 가능성 자동 보장 |
| D5 | **컨텍스트마다 `*-api` 공개 계약 모듈.** 소비자는 생산자의 `*-api`(port-in 인터페이스+DTO)만 의존 | 코어(domain/application)는 외부 컨텍스트를 절대 import 안 함. 추출 시 `*-api`가 그대로 HTTP 클라이언트 계약이 됨 |
| D6 | **이벤트 인프라: SNS→SQS 팬아웃 + transactional outbox + 멱등 inbox consumer.** 처음부터 진짜 브로커 | 인메모리 이벤트 두 코드경로 금지. 추출 시 새 실패모드 도입 없음(이미 테스트된 길). 비용: 같은 JVM 내도 브로커 지연/최종 일관성 — 의식적 선택 |
| D7 | **인증은 gateway 엣지.** authhub와 토큰 검증 → 서명된 내부 JWT 발급 → 다운스트림 주입. 다운스트림은 재검증 안 함, authhub 의존 금지 | 서비스마다 인증 팬아웃(안티패턴) 제거. `Actor` VO는 shared-kernel, port-in 커맨드에 명시 전달(숨은 ThreadLocal 금지) |
| D8 | **내부/외부 API = 코어 1개 위의 입력 어댑터 2개**(rest-internal BFF / rest-public OpenAPI). 배포 분리는 bootstrap 조립 | public DTO는 버전드, 도메인 타입 노출 금지(ACL). 같이 띄우든 따로 띄우든 어댑터 모듈은 day-one 분리 |
| D9 | **같은 DB 인스턴스, 서비스별 스키마 분리, 크로스 JOIN 없음.** 영속 어댑터는 자기 스키마만 | 추출 = DB만 나누면 됨. 스키마 분리는 앱 마이그레이션 도구(Flyway 등) 책임 |
| D10 | **공통 플랫폼 자산은 재사용 우선**(platform-commons-connectly / platform-bootstrap / terraform-modules). archrules는 SSOT 통합 | 3중 SSOT(ryuqqq/connectly/archrules-platform) 위험 → 본 archrules-platform로 일원화 |
| D11 | **groupId/네이밍은 `com.connectly`로 표준화.** 단 공통 라이브러리 배포는 **GitLab → GitHub**(Packages/JitPack)로 우선 전환. 인프라는 connectly 그대로 사용 | 흩어진 서비스들이 현재 `com.ryuqqq`로 보이나 마이그레이션 때 `com.connectly`로 통일. 배포 외부만 GitHub 우선. ryuqqq로의 re-baseline은 철회 |

---

## 2. 모듈 토폴로지

```
connectly-services/
├─ services/
│  ├─ marketplace/                         ← 서비스(배포 후보 묶음, 1 bootstrap + 1 DB 스키마군)
│  │  ├─ product/
│  │  │  ├─ product-domain/                 도메인 순수(애그리거트/VO/Id/예외/이벤트)
│  │  │  ├─ product-application/            port-in(UseCase)·port-out·service·manager
│  │  │  ├─ product-api/                    ★ 공개 계약(port-in 인터페이스 + DTO)만
│  │  │  ├─ product-adapter-persistence/    Testcontainers 대상, 자기 스키마만
│  │  │  └─ product-adapter-messaging/      outbox relay / consumer
│  │  ├─ order/   { -domain, -application, -api, -adapter-*, -adapter-order-product(다리) }
│  │  ├─ claim/   { ... }
│  │  └─ shipment/ settlement/ brand/ ...   (컨텍스트 목록은 마이그레이션 시 확정)
│  ├─ crawlinghub/  { 같은 골격 }
│  ├─ fileflow/     { 같은 골격 }
│  └─ authhub/      { 같은 골격 }
├─ gateway/                                 ← 엣지(인증/인가/라우팅), 헥사곤 예외
├─ shared-kernel/                           ← 역의존 0, 표면 최소
├─ platform/
│  ├─ archrules/ (또는 외부 archrules-platform 의존)  ← §4 규칙 세트
│  ├─ outbox-core/                          공용 transactional outbox/relay/inbox 메커니즘
│  └─ observability/  autoconfig/  security-jwt/
├─ adapter-in/
│  ├─ rest-internal/  (BFF, 세션/엣지 신원)
│  └─ rest-public/    (OpenAPI, API-key, 버전드 DTO)
└─ bootstrap/
   ├─ all-in-one/                           ← 지금: 전부 한 ECS 태스크
   ├─ <service>-api/  <service>-worker/     ← 나중: 추출 대상에만
   └─ scheduler/  worker/   (relay·배치)
```

**3계층 정의(문서·규칙 전반 고정):**
- **Context** = 도메인 경계 = 모듈 묶음. 최소 분할 단위.
- **Service** = Context들의 묶음. *현재* 배포·DB·일관성을 공유하는 단위. 고정 아님, bootstrap 조립으로 변하는 뷰.
- **Bootstrap** = 런타임 조립 = ECS 태스크. 서비스 선을 늦고 싸게 바꾸는 손잡이.

> 모듈 입자도는 점진 적용 가능: 초기에는 "서비스당 단일 모듈 + 패키지 레이어"로 시작하고 ArchUnit 패키지 규칙으로 경계를 강제하다가, 추출이 임박한 컨텍스트부터 모듈로 승격해도 규칙의 모양은 동일하다.

---

## 3. Seam 모델 (모든 경계에 동일 적용)

컨텍스트 간 상호작용은 **딱 2종**만 허용. 그 외 직접 import는 전부 위반. `product↔order`든 `marketplace↔fileflow`든 규칙은 같다.

### S1 — 동기 읽기 포트
남의 데이터를 *읽기만* 할 때. 호출자가 포트를 소유하고, 다리(bridge) 어댑터가 생산자의 `*-api`를 통해 구현.

```java
// order-application/port/out  — order가 자기 필요를 선언(정의만)
public interface ProductStockPort { StockView getStock(ProductId id); }

// order-adapter-order-product  — 다리 어댑터(유일하게 양쪽을 아는 곳)
//   의존: order-application(포트) + product-api(계약)
@Component
class InProcessProductStockAdapter implements ProductStockPort {
    private final GetProductStockUseCase productUseCase;   // product-api
    public StockView getStock(ProductId id) { return productUseCase.getStock(id); }
}
// 추출 후: 같은 포트를 HttpProductStockAdapter로 교체 → order 코드 불변
```

- `order-domain`/`order-application`은 product를 **import하지 않는다**(product-api조차도. 자기가 정의한 포트만 앎).
- 다리 어댑터는 **소비자(order) 진영**에 둔다. 생산자(product)에 두지 않는다(생산자는 소비자를 몰라야 함).
- bootstrap이 런타임에 이 어댑터를 빈으로 끼운다.

### S2 — 도메인 이벤트 (브로커)
컨텍스트를 넘는 **모든 쓰기/전파**. transactional outbox → relay → SNS→SQS 팬아웃 → 멱등 consumer. (상세 §5)

### 금지 (= archrules 위반)
- 컨텍스트 A가 B의 `domain` / `application`(service·manager) / `internal` 을 직접 import.
- 컨텍스트를 넘는 **원자적 쓰기**(여러 컨텍스트 manager를 한 `@Transactional` facade에 모으기). → 최종 일관성을 못 견디면 그 둘은 한 컨텍스트.
- 어떤 컨텍스트가 authhub를 의존.
- 한 컨텍스트의 영속 어댑터가 다른 컨텍스트의 테이블에 접근.

### 트랜잭션 규칙
**한 트랜잭션 = (한 컨텍스트의) 한 애그리거트 + 그 outbox 행.** 예: cancel 흐름은 `Cancel` + `cancel_outbox`만 원자적으로 쓰고 커밋 → `CancelApproved` 이벤트 발행 → order/shipment가 각자 자기 트랜잭션으로 후속 처리.

---

## 4. archrules 규칙 세트

표기: 🟢 ArchUnit 강제 가능 / 🟡 부분(휴리스틱) / 🔵 PR 리뷰 게이트.

### C-1. 도메인 순수성 (기존 `domain-rules` 확장)
- 🟢 domain은 시각을 직접 읽지 않음(주입) — *이미 있음*
- 🟢 domain에 setter 없음, 생성자 private, `forNew/of/reconstitute` 팩토리 — *이미 있음*
- 🟢 domain은 Spring/JPA/security/다른 컨텍스트를 import 안 함(java/shared-kernel/자기 컨텍스트만)
- 🟢 domain은 `Actor`/principal 타입을 모름(인가는 application)

### C-2. 컨텍스트 내 헥사고날 레이어링 (`hexagonal-rules` 확장)
- 🟢 의존 방향 `domain ← application ← adapter ← bootstrap` (역방향 금지)
- 🟢 application은 자기 domain + 자기 port만 의존. 외부 컨텍스트 import 금지
- 🟢 `*Service`는 대응 `*UseCase`(port-in) 구현
- 🟡 `*Manager`는 정확히 1개 `*Port`(out)에만 의존, 비즈니스 분기 금지
- 🟢 `port/out` 하위 패키지 ∈ {command, query, client, event} 화이트리스트

### C-3. 컨텍스트 격리 (신규 `context-isolation-rules`) — 이 아키텍처의 심장
- 🟢 컨텍스트 A는 B의 `domain`/`application`/`internal`을 import 안 함
- 🟢 컨텍스트 간 소통은 B의 `*-api`(port-in) 또는 도메인 이벤트로만
- 🟢 다리(bridge) 어댑터만 양쪽(`A-application` + `B-api`)을 알 수 있음 — 그 외 위치 금지
- 🟢 생산자 `*-api` 모듈엔 인터페이스+DTO만(구현/도메인/JPA 없음)

### C-4. 인증/인가 (신규 `security-boundary-rules`)
- 🟢 어떤 컨텍스트도 authhub 의존 금지(gateway만)
- 🟡 서비스는 토큰을 검증하지 않음 — 주입된 `Actor`만 신뢰
- 🟢 `public` 입력 어댑터의 응답 DTO는 도메인 타입을 노출하지 않음
- 🟡 인가 로직은 application에만(domain/adapter 금지)

### C-5. 이벤트 / Outbox (신규 `messaging-rules`)
- 🔵 한 트랜잭션은 한 애그리거트 + 그 outbox 행만 씀
- 🟡 컨텍스트 넘는 원자적 쓰기 금지(다중 컨텍스트 manager를 한 `@Transactional`에 모으는 facade 금지)
- 🟡 브로커 consumer 진입점은 멱등 처리(inbox/상태가드)를 거침
- 🟢 이벤트 payload는 도메인 애그리거트가 아니라 published DTO
- 🟢 이벤트는 shared-kernel `EventEnvelope`로 감쌈

### C-6. 영속 격리 (신규 `persistence-rules`)
- 🟡 한 컨텍스트의 영속 어댑터는 자기 컨텍스트 테이블/스키마에만 접근
- 🟢 JPA 엔티티/매퍼는 영속 어댑터 모듈 밖으로 새지 않음(도메인=엔티티 분리)

### C-7. shared-kernel / 네이밍
- 🟢 shared-kernel은 어떤 컨텍스트도 import 안 함(역의존 0)
- 🟢 `*Adapter`(port 구현)·`*Port`(인터페이스)·`*UseCase`(port-in) 네이밍 일관

### gateway 예외
- gateway 모듈은 C-2/C-3 대상 제외. 전용 규칙: "gateway만 authhub 의존 가능", "gateway는 어떤 컨텍스트의 domain도 import 안 함".

---

## 5. 이벤트 / Outbox 메커니즘

```
[생산 컨텍스트: 한 트랜잭션]  애그리거트 저장 + outbox 행 INSERT  (원자적)
        │ commit
[Relay]  outbox 폴링 → SNS publish → outbox 행 SENT 마킹
[SNS 토픽] ──fan-out──▶ [SQS 큐(소비 컨텍스트별) + DLQ]
[소비 컨텍스트]  수신 → inbox 중복검사(멱등) → 자기 트랜잭션으로 처리
```

- **브로커: SNS→SQS 팬아웃.** (기존 sqs-client/sqs-consumer 자산 활용)
- **멱등 consumer 필수:** inbox 테이블(`processed_event_id` UNIQUE) 또는 상태 가드.
- **EventEnvelope(shared-kernel):** `eventId, eventType, occurredAt, aggregateType, aggregateId, version, traceId, payload`. payload는 published DTO(도메인 애그리거트 직렬화 금지).
- **순서:** 기본 standard 큐 + 멱등 + version 인지(낮은 version 늦게 오면 무시). 애그리거트 단위 엄격 순서 필요 시 FIFO + MessageGroupId=aggregateId.

---

## 6. 아이덴티티 전파

```
클라이언트 ──토큰──▶ [gateway]  authhub 검증(인증) + 엣지 인가
                      ▼ 서명된 내부 JWT(principal 클레임) 주입
              [downstream]  토큰 재검증 X, 주입된 principal만 신뢰
                      ▼  application이 Actor로 세분 인가
```

- gateway가 다운스트림에 **서명된 내부 JWT** 발급. 서비스는 **공개키(JWKS)로 오프라인 검증**(authhub 호출 0). 추출해도 그대로 동작(네트워크 신뢰 가정 불필요).
- inbound 어댑터가 신원을 `Actor`(shared-kernel VO)로 변환 → **port-in 커맨드에 명시 전달**.
- domain은 Actor/보안을 모름. 인가는 application.

---

## 7. 내부/외부 API

- **코어 1개(port-in) 위에 입력 어댑터 2개:** `adapter-in/rest-internal`(BFF, 화면 집계, 세션/엣지 신원) / `adapter-in/rest-public`(OpenAPI, API-key/OAuth, 레이트리밋, 버전드 DTO).
- public 어댑터의 요청/응답 DTO는 **자기 소유 버전드 계약** — 도메인 타입 직접 노출 금지(내부 리팩터가 파트너를 안 깨도록).
- **배포 분리는 bootstrap 조립.** 지금은 같이 띄워도, 어댑터 모듈은 day-one 분리 → 나중 `bootstrap-public-api` 별도 ECS 태스크로 무비용 분리.
- gateway가 표면별 라우팅·인증 분기(`/api/v1/public/**` ↔ 내부 경로).
- "두 종류의 api" 구분: `product-api`(컨텍스트 *사이* 인-프로세스 Java 계약) ≠ 외부 OpenAPI(파트너용 HTTP 계약).

---

## 8. 테스트 전략

| 레이어 | 종류 | 메커니즘 | 핵심 |
|---|---|---|---|
| domain | 순수 단위 | Spring 없음, fixture/Object Mother | 불변식·VO·상태전이. 최다 |
| application | 유즈케이스 단위 | port-out mock/fake, Spring 없음 | 오케스트레이션. 외부 컨텍스트는 포트 mock(=격리 증명) |
| adapter-persistence | 슬라이스 | Testcontainers MySQL | 매핑·쿼리. 자기 스키마만 |
| adapter-rest | 웹 슬라이스 | MockMvc, UseCase mock | 배선·DTO·인가. public은 버전드 DTO 계약 |
| 이벤트 | 통합 | outbox→relay→consumer | 멱등성·순서·envelope |
| 아키텍처 | **ArchUnit** | §4 규칙 세트 | **CI 게이트** |
| e2e | 크로스 컨텍스트 | integration-test + 브로커 | 핵심 흐름 소수 |

**특별 항목:**
- **seam 계약 테스트(CDC):** order가 `ProductStockPort`에 기대하는 계약을 고정 → product가 만족하는지 검증 → 추출 후 HTTP 클라이언트에 같은 계약 재실행 = 추출 견딤 자동 증명.
- **이벤트 계약/스키마 호환 테스트:** payload published language 하위호환 깨짐 탐지.
- **멱등·최종일관성 테스트:** 중복 전달 → 효과 1번, 낮은 version 늦게 → 무시.
- 안티패턴: 단위/유즈케이스에 풀 Spring 컨텍스트 금지, 유즈케이스가 다른 컨텍스트 실물 필요 = 격리 실패 신호.

---

## 9. gateway · shared-kernel 상세

### gateway (엣지, 헥사곤 예외)
책임: ① 인증(authhub와 토큰 검증 — 유일한 주체) ② 신원 변환(서명 내부 JWT 발급·주입) ③ 엣지 인가(경로 단위) ④ 라우팅(public/internal 분기) ⑤ 횡단(레이트리밋·쿼터·WAF·traceId 발급).

### shared-kernel (역의존 0, 표면 최소)
| 허용 | 금지 |
|---|---|
| 공통 ID 베이스, 공통 VO(Money 등) | 특정 컨텍스트 도메인/애그리거트 |
| `EventEnvelope`, `Actor`(principal VO) | 비즈니스 로직 |
| 공통 result 타입, 공통 예외 베이스 | Spring/JPA 의존(가능한 한) |

철칙: 새 타입 추가는 **PR 게이트**(2개 이상 컨텍스트가 정말 공유하는가?). 한 곳만 쓰면 그 컨텍스트로 내림.

---

## 10. 공통 플랫폼 자산 통합 전략 (Section F)

> 분석은 3개 병렬 에이전트 보고 기반. 자산별 판정과 통합 계획.

### 10.1 Spring 공통 라이브러리 — `platform-commons-connectly` + `spring-platform-commons`

**관계:** 중복 아님. `spring-platform-commons`(GitHub/ryuqqq, `com.ryuqqq.platform`, 14 모듈)가 인큐베이터, `platform-commons-connectly`(GitLab/connectly, `com.connectly.platform`, 6 모듈)가 `promote-module.sh`로 승격된 회사 베이스라인. 표면 넓이는 ryuqqq, 회사 정합성은 connectly.

**이번 단계 방향(확정, D11):** groupId는 **`com.connectly.platform` 유지**, 단 **배포 타깃은 GitHub로 우선 전환**(GitLab 폐기). 모듈 구성은 `platform-commons-connectly`가 이미 큐레이션한 6모듈 선정을 따르되, **소스/배포 라인은 GitHub(JitPack/GitHub Packages)로 통일**한다. `spring-platform-commons`(GitHub)에서 부족분을 가져올 땐 `com.ryuqqq`→`com.connectly` 리네임 후 채택.

**판정:**
- `platform-commons-connectly` → **보완 후 사용(베이스라인).** common-domain/exception/archrules/outbox-relay/persistence-jpa는 거의 그대로 채택. **배포만 GitHub로 이전.**
- `spring-platform-commons` → **부분 흡수(리네임 채택).** `platform-web`의 `RequestContextFilter`(traceId 인바운드 전파)·`ApiResponse`·error 스택, `resilient-client`(외부 호출 회복탄력성)를 `com.connectly`로 리네임해 선별 승격.
- `spring-platform-commons`의 `platform-security` → **흡수 금지(참고만).** `X-Service-Token` 공유시크릿 모델이 우리 **서명-JWT 설계와 충돌**.

**가장 큰 공백(직접 작성 필요):** 둘 다 outbox는 **relay 골격 + `OutboxStore` SPI만** 있고 다음이 전부 없음 →
1. transactional outbox **DB store 구현체**
2. **SNS 발행 adapter**
3. 멱등 consumer용 **inbox 테이블/필터**
4. **EventEnvelope**(grep 0건)
5. **Actor VO + 서명 JWT 검증 필터**
6. connectly 쪽 **인바운드 traceId 필터 부재**(ryuqqq `RequestContextFilter`로 보완)

**통합 단계:**
1. connectly 6개 모듈을 connectly-services `platform/`·`shared-kernel`의 베이스라인으로 배치 + **GitHub 배포 라인 구성**(GitLab CI 폐기, JitPack/GitHub Packages).
2. ryuqqq에서 `RequestContextFilter`(+web error 스택)·`resilient-client` 선별 흡수.
3. shared-kernel에 `EventEnvelope`·`Actor` VO 신규 작성(순수 record/VO, 검증 로직 없음).
4. `platform/outbox-core`에 DB store / SNS adapter / inbox 신규 작성(SPI 채우기) — **최대 작업**.
5. `platform/security-jwt`에 서명 JWT 검증 + principal 주입 필터 신규(authhub 비의존).
6. archrules SSOT를 본 `archrules-platform`과 통합(아래 10.4).

### 10.2 인프라 토대 — `platform-bootstrap` (Terraform L0/L1)

**정체:** state backend·공유 VPC(import 완료)·공유 MySQL RDS+Proxy·관측성(AMP/AMG)·ops-alerts(SNS)·security(GuardDuty)·finops. 전체 ~85% 완성(단 **L2 ECS 워크로드는 0%**). `terraform-modules`를 git 태그로 소비하는 깔끔한 2계층. ADR 4건이 이미 5개 서비스를 L2로 상정.

**판정: 재사용 + 보완. connectly-services 레포로 흡수하지 말고 별도 인프라 레포로 유지.** (ADR-0001이 L0/L1을 blast-radius 격리 목적으로 분리. connectly-services 인프라는 L2로 `infra/`에 두고 bootstrap remote state를 참조.)

**보완(connectly-services 띄우기 전 필수):**
1. **앱 이벤트 인프라 신설** — SNS→SQS 팬아웃 + DLQ + IAM (※ ops-alerts SNS는 *운영 알림용*이지 앱 버스 아님 — 혼동 주의).
2. **L2 ECS 워크로드 패키지** — all-in-one task-def(서비스 컨테이너들) + ecs-service. network/shared-data/observability output을 remote state 참조.
3. **ALB + gateway 엣지.**
4. **DB 스키마 분리** — 공유 MySQL에 서비스별 스키마/유저, **Proxy 경유**(ADR-0001 직접접근 금지).
5. **ADOT 사이드카** 부착(traceId 계측은 앱 책임).
6. platform-gitops `repos.yaml`에 등록.

### 10.3 재사용 Terraform 모듈 — `terraform-modules` (51 모듈)

**정체:** 전 모듈 구조 100%(7종 파일+semver 태그+examples). 핵심 5종 존재: ECS(cluster/task-def/service), ALB, SNS, SQS(DLQ 내장), RDS. 전체 ~88%.

**판정: 재사용(그대로 소비) + 소비처 wrapper 보강.** 모듈 자체 수정 거의 불필요. "1태스크→N태스크 추출"은 `for_each`/모듈블록 복제로 파라미터화 가능.

**보강(소비처 wrapper):**
- **SNS→SQS queue policy 자동배선 공백** — 토픽 ARN→SendMessage 허용 policy를 wrapper에서 작성(큐 수만큼 반복 → 헬퍼 추출).
- `ecs-service` **코드 vs README 모순**(`ignore_changes=[task_definition]`이 코드엔 적용, README엔 미적용) — **소비 전 코드 확인 필수**.
- sns/sqs validation 0개 — wrapper 입력 검증 보완.
- (선택) 모듈 측 개선 PR: sqs `allowed_sns_topic_arns` 편의 변수, 팬아웃 example.

### 10.4 SSOT 정리 — archrules 삼중화 해소

같은 `HexagonalArchRules`/도메인 규칙이 **ryuqqq, connectly, 본 archrules-platform** 세 곳에 존재/발전 중. → **본 `archrules-platform`을 단일 SSOT로** 확정하고, connectly-services는 이를 테스트 의존성으로 소비. commons 쪽 archrules 모듈은 재노출/얇은 위임만.

---

## 11. 리스크 / 주의 (통합)

1. **이벤트 인프라는 사실상 미존재** — commons의 "outbox 있음"은 착시(relay 골격+SPI만). 핵심(EventEnvelope·DB store·SNS·inbox 멱등)은 거의 신규 작성.
2. **인증 모델 충돌** — ryuqqq `platform-security` 공유시크릿을 무비판 흡수하면 서명-JWT 설계와 어긋남. authhub 의존 유입 주의.
3. **삼중 SSOT(archrules)** — 통합 시점 못 정하면 규칙 분기.
4. **승격 파이프라인 + 배포 이전** — connectly는 ryuqqq에서 수동 승격(`promote-module.sh`). 이번에 **배포를 GitLab→GitHub로 옮기므로** 좌표(groupId 유지, 레지스트리 변경)·CI 재구성 필요. upstream(ryuqqq) 추적/포크 단절 정책도 결정.
5. **공유 prod VPC/RDS는 ECS 16클러스터가 사용 중** — connectly-services는 기존 운영 인프라에 얹힘. 입양 리소스 `ignore_changes`(password·ingress) 깨지 말 것, 신규 서비스는 Proxy 경유.
6. **all-in-one 1태스크 ↔ ADR 전제 재검토** — bootstrap ADR은 서비스당 1 ECS 서비스 전제. `ecs-task-definition` 다중 컨테이너 지원 여부 확인 필요(`main_container`+`sidecars[]` 구조 점검).
7. **모듈러 모놀리식 함정** — 컨텍스트 간 직접 import 1건이라도 새면 "분산 빅머드볼"로 퇴화. C-3 규칙을 CI 첫날부터 강제.

---

## 12. 다음 단계

1. 본 스펙 사용자 리뷰 → 확정.
2. `writing-plans`로 구현 계획 수립. 우선순위 후보:
   - (P0) archrules 규칙 세트 §4 구현 + SSOT 통합(가장 먼저, 이후 모든 코드가 이 위에서 자람).
   - (P0) `platform/outbox-core`(DB store/SNS adapter/inbox) + `shared-kernel`(EventEnvelope/Actor) 신규.
   - (P1) `platform/security-jwt` 서명 JWT 검증·주입.
   - (P1) 인프라 L2(이벤트 SNS→SQS, all-in-one ECS, ALB/gateway, 스키마 분리) wrapper.
   - (P2) 서비스별 컨텍스트 모듈 마이그레이션(marketplace product/order/claim … 부터). 이때 흩어진 서비스 패키지 `com.ryuqqq.*` → `com.connectly.*` 표준화 동반.
3. 컨텍스트 목록 확정(marketplace 내부: product/order/claim/shipment/settlement/brand/category…)은 마이그레이션 착수 시 별도 세션.
