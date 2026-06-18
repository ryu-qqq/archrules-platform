package com.ryuqqq.archrules.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchRulesCliTest {

    @Test
    void writesReportAndReportOnlyByDefault(@TempDir Path tmp) throws Exception {
        // FixtureRulesService(위반 발생)가 test 클래스패스에 등록돼 있음.
        Path classesDir = Path.of("build/classes/java/test");
        Path report = tmp.resolve("report.md");

        ArchRulesCli.CliOutcome outcome = ArchRulesCli.execute(classesDir, report, false);

        assertTrue(Files.exists(report), "리포트 파일 생성");
        assertTrue(outcome.rulesRun() >= 1, "규칙이 발견됨");
        assertTrue(outcome.failures() >= 1, "위반 집계");
        assertEquals(0, outcome.exitCode(), "report-only(exit 0)");
    }

    @Test
    void gatesWhenFailOnViolation(@TempDir Path tmp) throws Exception {
        Path classesDir = Path.of("build/classes/java/test");
        Path report = tmp.resolve("report.md");

        ArchRulesCli.CliOutcome outcome = ArchRulesCli.execute(classesDir, report, true);

        assertEquals(1, outcome.exitCode(), "failOnViolation + 위반 → exit 1");
    }

    @Test
    void exitCodeMapping() {
        assertEquals(2, ArchRulesCli.mapExitCode(0, 0, false));  // 규칙 0개 → silent-skip 가드
        assertEquals(2, ArchRulesCli.mapExitCode(0, 0, true));   // 규칙 0개는 게이트 무관 가드
        assertEquals(0, ArchRulesCli.mapExitCode(3, 2, false));  // report-only
        assertEquals(1, ArchRulesCli.mapExitCode(3, 2, true));   // 게이트 + 위반
        assertEquals(0, ArchRulesCli.mapExitCode(3, 0, true));   // 게이트지만 위반 없음
    }
}
