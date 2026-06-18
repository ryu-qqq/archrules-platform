package com.ryuqqq.archrules.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.lang.Priority;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchRulesCliTest {

    @Test
    void reportOnlyWhenNoThreshold(@TempDir Path tmp) throws Exception {
        Path classesDir = Path.of("build/classes/java/test");
        Path report = tmp.resolve("report.md");

        ArchRulesCli.CliOutcome outcome = ArchRulesCli.execute(classesDir, report, null);

        assertTrue(Files.exists(report), "리포트 파일 생성");
        assertTrue(outcome.rulesRun() >= 1, "규칙 발견");
        assertTrue(outcome.failures() >= 1, "위반 집계");
        assertEquals(0, outcome.exitCode(), "threshold 없으면 report-only");
    }

    @Test
    void gatesWhenHighViolationMeetsThreshold(@TempDir Path tmp) throws Exception {
        // fixture "no public Banned"는 HIGH 위반.
        Path classesDir = Path.of("build/classes/java/test");
        Path report = tmp.resolve("report.md");

        ArchRulesCli.CliOutcome outcome = ArchRulesCli.execute(classesDir, report, Priority.HIGH);

        assertEquals(1, outcome.exitCode(), "HIGH 위반이 HIGH threshold 이상 → exit 1");
    }

    @Test
    void exitCodeMapping() {
        assertEquals(2, ArchRulesCli.mapExitCode(0, 0, null));          // 0 규칙 → 가드
        assertEquals(2, ArchRulesCli.mapExitCode(0, 0, Priority.HIGH)); // 0 규칙은 게이트 무관
        assertEquals(0, ArchRulesCli.mapExitCode(3, 2, null));          // report-only
        assertEquals(1, ArchRulesCli.mapExitCode(3, 2, Priority.HIGH)); // 게이트 위반 있음
        assertEquals(0, ArchRulesCli.mapExitCode(3, 0, Priority.HIGH)); // 게이트 위반 없음
    }

    @Test
    void gateFailuresCountsOnlyAtOrAboveThreshold() {
        // HIGH 위반 1 + LOW 위반 1, threshold=HIGH → gateFailures=1 (LOW는 threshold 미만).
        java.util.List<RuleResult> results = java.util.List.of(
                new RuleResult("h", Priority.HIGH, true, java.util.List.of("v")),
                new RuleResult("l", Priority.LOW, true, java.util.List.of("v")));
        assertEquals(1, ArchRulesCli.gateFailures(results, Priority.HIGH));
        assertEquals(1, ArchRulesCli.gateFailures(results, Priority.MEDIUM)); // HIGH 게이트, LOW 미게이트
        assertEquals(2, ArchRulesCli.gateFailures(results, Priority.LOW));
        assertEquals(0, ArchRulesCli.gateFailures(results, null));
    }
}
