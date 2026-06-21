# archrules-platform

Netflix [Nebula ArchRules](https://github.com/nebula-plugins/nebula-archrules)를 보고 착안한, **외부 빌드 의존 0**의 경량 [ArchUnit](https://www.archunit.org/) 규칙 플랫폼. SPI로 규칙을 저작하고 `ServiceLoader`로 발견·실행한다 — Gradle 플러그인·variant 같은 무거운 기계장치 없이 직접 구현.

- **좌표**: `com.github.ryu-qqq.archrules-platform:<module>:0.2.0` (JitPack)
- **런타임 의존**: ArchUnit + 우리 SPI 뿐
- **설계 근거**: [`docs/adr/0001`](docs/adr/0001-archrules-platform-positioning-and-make-vs-buy.md) — 엔진은 Nebula식만 흡수, 룰은 어노테이션-프리 상대매처(jMolecules·Spring Modulith 비채택).

## 엔진

| 모듈 | 역할 |
|------|------|
| `archrules-api` | SPI — `ArchRulesService`(규칙 노출) + `ArchRuleSpec`(rule+priority) |
| `archrules-runtime` | 발견·실행·markdown 리포트·CLI·baseline ratchet(기존 위반 동결, 신규만 게이트) |
| `archrules-common` | 중립 프리미티브 `AppPackages`(서드파티 false-positive 차단용 앱-베이스 판정) |

## 룰즈 (C-1 ~ C-7)

| 모듈 | 강제하는 것 |
|------|------|
| `domain-rules` (C-1) | 도메인 순수성 — 시각 비의존·setter 금지·private 생성자+정적팩토리·VO/aggregate 컨벤션 |
| `hexagonal-rules` (C-2) | 헥사고날 — 레이어 방향(domain←application←adapter←bootstrap)·프레임워크 비의존 |
| `context-isolation-rules` (C-3) | 컨텍스트 격리 — 남의 internal 직접 import 금지, 교차는 `.api`/이벤트로만 |
| `security-boundary-rules` (C-4) | authhub는 gateway만 의존·public 어댑터 DTO 도메인 비노출 |
| `messaging-rules` (C-5) | 이벤트 payload는 애그리거트가 아닌 published DTO |
| `persistence-rules` (C-6) | JPA 엔티티가 영속 어댑터 밖으로 누출 금지 |
| `shared-kernel-rules` (C-7) | shared-kernel 역의존 0 |

## 쓰기

```gradle
repositories { maven { url 'https://jitpack.io' } }
dependencies {
  testImplementation 'com.github.ryu-qqq.archrules-platform:domain-rules:0.2.0'
  testImplementation 'com.github.ryu-qqq.archrules-platform:archrules-runtime:0.2.0'
}
```

CI에선 `ArchRulesCli`로 컴파일된 클래스에 규칙을 돌려 markdown 리포트 + 게이트(`--threshold`, `--baseline`):
```bash
java -cp <runtime+규칙 jar> com.ryuqqq.archrules.runtime.ArchRulesCli \
  --classes build/classes/java/main --report build/reports/archrules/report.md --threshold HIGH
```
규칙 0개 발견 시 exit 2(적용 누락 가드), threshold 이상 위반 시 exit 1, 그 외 0.

**도메인 특화 규칙**은 소비 레포가 `ArchRulesService`를 구현해 엔진 위에 직접 추가한다(ServiceLoader가 공통 규칙과 함께 발견).

## 규칙 작성 컨벤션

모든 규칙은 priority + `.as(name)` + `.because(reason)` + `.allowEmptyShould(true)`, **상대 패키지 매처(`..domain..`)만** 사용(root 비하드코딩). 각 모듈의 `RuleConventionTest`가 self-test로 강제.

## 배포

git **태그**를 밀면 JitPack이 빌드: `com.github.ryu-qqq.archrules-platform:<module>:<tag>`. 로컬 발행은 `./gradlew publishToMavenLocal -ParchrulesVersion=<v>`.
