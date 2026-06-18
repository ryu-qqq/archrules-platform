package com.ryuqqq.archrules.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** CI 진입점 — 컴파일된 소비자 클래스에 규칙을 직접 실행한다. */
public final class ArchRulesCli {

    private ArchRulesCli() {}

    public record CliOutcome(int exitCode, String markdown, int rulesRun, int failures) {}

    public static CliOutcome execute(Path classesDir, Path reportOut, boolean failOnViolation)
            throws Exception {
        List<RuleResult> results = ArchRulesRunner.run(classesDir);
        String md = ArchRulesReport.toMarkdown(results);
        if (reportOut != null) {
            Files.createDirectories(reportOut.toAbsolutePath().getParent());
            Files.writeString(reportOut, md);
        }
        int failures = (int) results.stream().filter(RuleResult::hasViolation).count();
        int exit = mapExitCode(results.size(), failures, failOnViolation);
        return new CliOutcome(exit, md, results.size(), failures);
    }

    /** 0 규칙 → 2(silent-skip 가드). failOnViolation + 위반 → 1. 그 외 → 0. */
    static int mapExitCode(int rulesRun, int failures, boolean failOnViolation) {
        if (rulesRun == 0) return 2;
        if (failOnViolation && failures > 0) return 1;
        return 0;
    }

    public static void main(String[] args) throws Exception {
        Path classes = Path.of("build/classes/java/main");
        Path report = Path.of("build/reports/archrules/report.md");
        boolean failOnViolation = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--classes" -> classes = Path.of(args[++i]);
                case "--report" -> report = Path.of(args[++i]);
                case "--fail-on-violation" -> failOnViolation = true;
                default -> { }
            }
        }
        CliOutcome outcome = execute(classes, report, failOnViolation);
        System.out.println(outcome.markdown());
        System.exit(outcome.exitCode());
    }
}
