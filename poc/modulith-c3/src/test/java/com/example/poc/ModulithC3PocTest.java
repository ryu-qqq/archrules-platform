package com.example.poc;

import com.example.poc.compliant.ComplianceApp;
import com.example.poc.violating.ViolatingApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring Modulith C-3 PoC 검증 테스트.
 *
 * 전략 등록: test/resources/application.properties
 *   spring.modulith.detection-strategy=com.example.poc.strategy.TwoLevelContextDetectionStrategy
 *
 * ApplicationModules.of(Class) 가 내부적으로 ApplicationModuleDetectionStrategyLookup을 통해
 * 프로퍼티를 읽고 TwoLevelContextDetectionStrategy 를 인스턴스화.
 *
 * 성공기준:
 * 1. compliantStructureVerifies      — verify() 예외 없이 통과
 * 2. violatingStructureFailsVerification — verify() 가 위반 예외 발생
 * 3. 어노테이션(@ApplicationModule/@NamedInterface) 0 — 소스 파일에 없음
 * 4. 부작용(오탐 사이클 등) 관찰 — compliant verify() 결과로 확인
 */
class ModulithC3PocTest {

    // ── 기준 1: COMPLIANT 구조는 통과해야 함 ─────────────────────────────────
    @Test
    void compliantStructureVerifies() {
        ApplicationModules modules = ApplicationModules.of(ComplianceApp.class);

        System.out.println("=== COMPLIANT 모듈 목록 ===");
        modules.forEach(m -> System.out.println(
                "  모듈: " + m.getIdentifier()
                        + " | named interfaces: " + m.getNamedInterfaces()
                        + " | base: " + m.getBasePackage()
        ));

        // verify() 예외 없이 통과해야 함 (기준 1)
        Executable verifyCall = modules::verify;
        assertDoesNotThrow(verifyCall, "COMPLIANT 구조는 verify()를 통과해야 합니다 (기준 1)");
    }

    // ── 기준 2: VIOLATING 구조는 위반을 감지해야 함 ─────────────────────────
    @Test
    void violatingStructureFailsVerification() {
        ApplicationModules modules = ApplicationModules.of(ViolatingApp.class);

        System.out.println("=== VIOLATING 모듈 목록 ===");
        modules.forEach(m -> System.out.println(
                "  모듈: " + m.getIdentifier()
                        + " | named interfaces: " + m.getNamedInterfaces()
                        + " | base: " + m.getBasePackage()
        ));

        // verify() 가 위반(order->product.domain 직접 참조)을 감지해야 함 (기준 2)
        Exception ex = assertThrows(Exception.class,
                (Executable) modules::verify,
                "VIOLATING 구조는 verify()에서 예외가 발생해야 합니다 (기준 2)");

        System.out.println("=== 발생한 예외 (기대된 위반) ===");
        System.out.println(ex.getMessage());

        // 위반 메시지에 order 또는 product 관련 내용이 있어야 함
        assertTrue(
                ex.getMessage() != null && (
                        ex.getMessage().contains("order") || ex.getMessage().contains("product")
                                || ex.getMessage().contains("violation")
                ),
                "위반 메시지에 order 또는 product가 포함되어야 합니다: " + ex.getMessage()
        );
    }

    // ── 기준 4: 부작용 관찰 (오탐 사이클/엉뚱한 에러) ────────────────────────
    @Test
    void observeSideEffectsCompliant() {
        ApplicationModules compliant = ApplicationModules.of(ComplianceApp.class);

        System.out.println("=== 부작용 관찰: 모듈 상세 ===");
        compliant.forEach(m -> {
            System.out.println("모듈: " + m.getIdentifier());
            System.out.println("  base package: " + m.getBasePackage());
            System.out.println("  named interfaces: " + m.getNamedInterfaces());
        });

        // COMPLIANT에서 오탐 예외가 나면 부작용 존재
        try {
            compliant.verify();
            System.out.println("=== 부작용 없음: 사이클·오탐 감지 없음 ===");
        } catch (Exception e) {
            System.out.println("=== 예상치 못한 예외(부작용 의심) ===");
            System.out.println(e.getMessage());
            fail("COMPLIANT 구조에서 예상치 못한 예외 발생 — 오탐 또는 API 부작용: " + e.getMessage());
        }
    }

    // ── 기준 3 보조: 모듈 개수 및 명칭 확인 (2단 평면화 동작 확인) ─────────────
    @Test
    void twoLevelStrategyFlattensModulesCorrectly() {
        ApplicationModules modules = ApplicationModules.of(ComplianceApp.class);

        long moduleCount = modules.stream().count();
        System.out.println("=== 감지된 모듈 수: " + moduleCount + " ===");
        modules.forEach(m -> System.out.println("  - " + m.getIdentifier() + " @ " + m.getBasePackage()));

        // marketplace.product 와 marketplace.order 가 각각 독립 모듈로 잡혀야 함
        assertTrue(moduleCount >= 2,
                "최소 product, order 2개 모듈이 감지되어야 합니다. 실제: " + moduleCount);

        boolean hasProduct = modules.stream()
                .anyMatch(m -> m.getIdentifier().toString().contains("product")
                        || m.getBasePackage().getName().contains("product"));
        boolean hasOrder = modules.stream()
                .anyMatch(m -> m.getIdentifier().toString().contains("order")
                        || m.getBasePackage().getName().contains("order"));

        assertTrue(hasProduct, "product 모듈이 감지되어야 합니다");
        assertTrue(hasOrder, "order 모듈이 감지되어야 합니다");
    }
}
