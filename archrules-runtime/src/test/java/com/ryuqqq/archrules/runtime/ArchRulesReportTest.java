package com.ryuqqq.archrules.runtime;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ArchRulesReportTest {

    @Test
    void markdownContainsRuleSummaryAndViolations() {
        List<RuleResult> results = List.of(
                new RuleResult("layer boundary", com.tngtech.archunit.lang.Priority.HIGH, true,
                        List.of("Class A depends on B")),
                new RuleResult("no setters", com.tngtech.archunit.lang.Priority.MEDIUM, false, List.of()));

        String md = ArchRulesReport.toMarkdown(results);

        assertTrue(md.contains("layer boundary"), "규칙명 포함");
        assertTrue(md.contains("Class A depends on B"), "위반 상세 포함");
        assertTrue(md.contains("no setters"), "통과 규칙도 포함");
        assertTrue(md.contains("FAIL"), "상태 표시");
        assertTrue(md.contains("HIGH"), "priority 표시");
    }

    @Test
    void emptyResultsProducesNoRulesNotice() {
        String md = ArchRulesReport.toMarkdown(List.of());
        assertTrue(md.toLowerCase().contains("no rules"), "0 규칙 안내 포함");
    }

    @Test
    void escapesPipesAndNewlinesInViolations() {
        List<RuleResult> results = List.of(
                new RuleResult("r", com.tngtech.archunit.lang.Priority.HIGH, true,
                        List.of("A depends on B | extra\nsecond line")));

        String md = ArchRulesReport.toMarkdown(results);

        // 위반 상세 줄에 raw 파이프/개행이 그대로 남지 않는다.
        assertTrue(md.contains("A depends on B \\| extra"), "파이프 이스케이프");
        assertTrue(!md.contains("extra\nsecond line"), "개행은 공백으로 치환");
    }
}
