# PoC: Spring Modulith C-3 어노테이션-프리 강제 가능성 검증

**날짜**: 2026-06-20
**판정**: **VIABLE** — 어노테이션(@ApplicationModule/@NamedInterface) 0으로 C-3(컨텍스트 격리) 정적 강제 가능

---

## 실행 환경

- Java 21 (OpenJDK 21.0.10 Homebrew)
- Gradle 8.14.3 (부모 프로젝트 gradlew 복사 사용)
- spring-modulith-core:2.1.0 + spring-boot-autoconfigure:4.1.0 + spring-framework:7.0.8
- 독립 standalone 프로젝트 (poc/modulith-c3/), 루트 settings.gradle에 포함 안 됨

---

## 성공기준 1~4 실측 결과

### 기준 1: compliantStructureVerifies() — PASS

```
ModulithC3PocTest > compliantStructureVerifies() PASSED
```

ApplicationModules.of(ComplianceApp.class).verify() 예외 없이 통과.
- marketplace.product 모듈: named interface api (ProductApi 노출), domain internal
- marketplace.order 모듈: product.api.ProductApi만 참조 → 통과

### 기준 2: violatingStructureFailsVerification() — PASS

```
ModulithC3PocTest > violatingStructureFailsVerification() PASSED
```

위반 메시지 (실측):
```
- Module 'marketplace.order' depends on non-exposed type
  com.example.poc.violating.marketplace.product.domain.Product
  within module 'marketplace.product'!
  Method <...OrderService.describeOrder(...)> calls constructor
  <...Product.<init>(...)> in (OrderService.java:13)
- Module 'marketplace.order' depends on non-exposed type ... calls method
  <...Product.getName()> in (OrderService.java:14)
```

order -> product.domain.Product 직접 참조를 정확히 감지하고 예외 발생.

### 기준 3: 어노테이션 0 달성 — PASS

- @ApplicationModule 사용: 0건
- @NamedInterface 사용: 0건
- named interface 식별: 패키지 경로(.api) + TwoLevelContextDetectionStrategy.detectNamedInterfaces()의
  NamedInterfaces.builder(pkg).matching("api").build() 만으로 달성

### 기준 4: 부작용 관찰 — 없음

```
ModulithC3PocTest > observeSideEffectsCompliant() PASSED
=== 부작용 없음: 사이클·오탐 감지 없음 ===
```

- COMPLIANT 구조에서 오탐 사이클·엉뚱한 에러 없음
- #182 류 커스텀 전략이 사이클 탐지를 깨뜨리는 현상 미관찰

---

## 커스텀 전략 구현 결과

### TwoLevelContextDetectionStrategy

```java
@Override
public Stream<JavaPackage> getModuleBasePackages(JavaPackage basePackage) {
    // basePackage(com.example.poc.compliant) 의 직속 하위(marketplace)
    // → 그 직속 하위(product, order) = 독립 모듈
    return basePackage.getDirectSubPackages()
            .stream()
            .flatMap(svcPackage -> svcPackage.getDirectSubPackages().stream());
}

@Override
public NamedInterfaces detectNamedInterfaces(JavaPackage moduleBasePackage,
                                              ApplicationModuleInformation information) {
    return NamedInterfaces.builder(moduleBasePackage)
            .recursive()
            .matching("api")
            .build();
}
```

2단 깊이 평면 모듈 목록 성공:
- com.example.poc.compliant.marketplace.product → 독립 모듈
- com.example.poc.compliant.marketplace.order   → 독립 모듈

---

## API 마찰 (실측)

1. **버전 불일치 함정**: spring-modulith-core:2.1.0은 spring-boot:4.1.0 + spring-framework:7.0.8을 요구.
   spring-boot:3.4.x로 묶으면 ConfigDataEnvironmentPostProcessor.applyTo() NoSuchMethodError 발생.
   문서상 명시 부족 — pom 직접 파악 필요.

2. **오버로드 부재**: ApplicationModules.of(Class, ApplicationModuleDetectionStrategy) 직접 주입 오버로드 없음.
   전략은 반드시 test/resources/application.properties의
   spring.modulith.detection-strategy=<FQCN> 프로퍼티로만 등록.
   테스트 간 전략 교체 불가.

3. **NamedInterfaces.builder() 시그니처**: 문서 예제와 실제 API 불일치.
   실제: builder(JavaPackage) 팩토리, build()에 인자 없음.

4. **ApplicationModule.getName() 부재**: getIdentifier().toString() 사용 필요.

---

## 트레이드오프 요약

| 항목 | Spring Modulith 커스텀 전략 | 우리의 ArchUnit 직접 규칙 |
|------|----------------------------|-----------------------------|
| 어노테이션 | 0 | 0 |
| 런타임 의존 | Spring Boot 4.x 강제 | ArchUnit만 |
| 버전 결합 | Spring Boot 메이저 주기 종속 | 없음 |
| named interface 방식 | .api 패키지 경로 자동 탐지 성공 | 패키지 경로 규칙 직접 표현 |
| 오류 메시지 | Modulith 기본 메시지 (명확) | 커스텀 가능 |
| 빌드 오염 위험 | Spring 의존 유입 위험 (격리 필요) | 없음 |

**핵심 결론**: VIABLE이지만 Spring Boot 4.x 의존 강제가 현재 archrules-platform(Spring 런타임 의존 없음)에 큰 비용.
현재 ArchUnit 직접 규칙이 의존 경량화·버전 독립성·커스텀 메시지 면에서 우위.
Modulith는 이미 Spring Boot 4.x 기반 프로젝트에서 보조 검증 레이어로 추가하는 시나리오에 적합.
