package com.ryuqqq.archrules.runtime;

import com.tngtech.archunit.lang.Priority;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** CI 진입점 — 컴파일된 소비자 클래스에 규칙을 직접 실행한다. */
public final class ArchRulesCli {

    private ArchRulesCli() {}

    public record CliOutcome(int exitCode, String markdown, int rulesRun, int failures) {}

    public static CliOutcome execute(Path classesDir, Path reportOut, Priority threshold)
            throws Exception {
        List<RuleResult> results = ArchRulesRunner.run(classesDir);
        String md = ArchRulesReport.toMarkdown(results);
        if (reportOut != null) {
            Files.createDirectories(reportOut.toAbsolutePath().getParent());
            Files.writeString(reportOut, md);
        }
        int failures = (int) results.stream().filter(RuleResult::hasViolation).count();
        int exit = mapExitCode(results.size(), gateFailures(results, threshold), threshold);
        return new CliOutcome(exit, md, results.size(), failures);
    }

    /** threshold 이상(같거나 더 강함) priority의 위반 수. threshold null이면 0. */
    static int gateFailures(List<RuleResult> results, Priority threshold) {
        if (threshold == null) return 0;
        return (int) results.stream()
                .filter(RuleResult::hasViolation)
                .filter(r -> r.priority().compareTo(threshold) <= 0)
                .count();
    }

    /** 0 규칙 → 2(silent-skip 가드). threshold 이상 위반 → 1. 그 외 → 0. */
    static int mapExitCode(int rulesRun, int gateFailures, Priority threshold) {
        if (rulesRun == 0) return 2;
        if (threshold != null && gateFailures > 0) return 1;
        return 0;
    }

    public static void main(String[] args) throws Exception {
        Path classes = Path.of("build/classes/java/main");
        Path report = Path.of("build/reports/archrules/report.md");
        Priority threshold = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--classes" -> classes = Path.of(requireValue(args, i++, "--classes"));
                case "--report" -> report = Path.of(requireValue(args, i++, "--report"));
                case "--threshold" -> {
                    String v = requireValue(args, i++, "--threshold");
                    threshold = ("none".equalsIgnoreCase(v) || "null".equalsIgnoreCase(v))
                            ? null : Priority.valueOf(v.toUpperCase());
                }
                default -> { }
            }
        }
        CliOutcome outcome = execute(classes, report, threshold);
        System.out.println(outcome.markdown());
        System.exit(outcome.exitCode());
    }

    /** 플래그 다음 인자를 반환. 없으면 usage 에러로 종료(exit 2). */
    private static String requireValue(String[] args, int flagIndex, String flag) {
        if (flagIndex + 1 >= args.length) {
            System.err.println("missing value for " + flag);
            System.exit(2);
        }
        return args[flagIndex + 1];
    }
}
