package com.ryuqqq.archrules.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FreezeRunnerTest {

    /** store 시스템 프로퍼티를 설정해 body를 실행하고 finally로 정리한다. body는 checked exception을 던지지 않는다. */
    private void withStore(Path dir, Runnable body) {
        System.setProperty("archunit.freeze.store.default.path", dir.toString());
        System.setProperty("archunit.freeze.store.default.allowStoreCreation", "true");
        try {
            body.run();
        } finally {
            System.clearProperty("archunit.freeze.store.default.path");
            System.clearProperty("archunit.freeze.store.default.allowStoreCreation");
        }
    }

    @Test
    void freeze_firstRun_freezesExistingViolationsAsBaseline(@TempDir Path store) throws Exception {
        JavaClasses baseline = new ClassFileImporter()
                .importClasses(com.ryuqqq.archrules.runtime.freezefixture.one.Banned.class);

        withStore(store, () -> {
            List<RuleResult> results = ArchRulesRunner.run(baseline, true);
            // 첫 실행: 기존 위반을 baseline으로 동결 → 보고되는 위반 0
            assertFalse(results.stream().anyMatch(RuleResult::hasViolation),
                    "freeze 첫 실행은 기존 위반을 동결해 통과시킨다");
        });
        // store 파일이 생성됐다
        try (var s = Files.list(store)) {
            assertTrue(s.findAny().isPresent(), "violation store 파일 생성");
        }
    }

    @Test
    void freeze_secondRun_failsOnlyOnNewViolation(@TempDir Path store) {
        JavaClasses baselineOnly = new ClassFileImporter()
                .importClasses(com.ryuqqq.archrules.runtime.freezefixture.one.Banned.class);
        JavaClasses baselinePlusNew = new ClassFileImporter()
                .importClasses(
                        com.ryuqqq.archrules.runtime.freezefixture.one.Banned.class,
                        com.ryuqqq.archrules.runtime.freezefixture.two.Banned.class);

        withStore(store, () -> {
            ArchRulesRunner.run(baselineOnly, true);                 // baseline 동결
            List<RuleResult> second = ArchRulesRunner.run(baselinePlusNew, true); // 신규 1개 추가
            assertTrue(second.stream().anyMatch(RuleResult::hasViolation),
                    "baseline에 없던 신규 위반은 실패로 보고된다");
        });
    }
}
