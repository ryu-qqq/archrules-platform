package com.ryuqqq.archrules.runtime;

import java.util.List;

public final class ArchRulesReport {

    private ArchRulesReport() {}

    private static String md(String s) {
        return s.replace("\r", " ").replace("\n", " ").replace("|", "\\|");
    }

    public static String toMarkdown(List<RuleResult> results) {
        StringBuilder sb = new StringBuilder("# ArchRules Report\n\n");
        if (results.isEmpty()) {
            sb.append("> No rules were discovered or run.\n");
            return sb.toString();
        }
        long violated = results.stream().filter(RuleResult::hasViolation).count();
        sb.append("Rules run: ").append(results.size())
          .append(" — with violations: ").append(violated).append("\n\n");
        sb.append("| Rule | Priority | Status | Violations |\n");
        sb.append("|------|----------|--------|------------|\n");
        for (RuleResult r : results) {
            sb.append("| ").append(md(r.ruleName()))
              .append(" | ").append(r.priority())
              .append(" | ").append(r.hasViolation() ? "FAIL" : "PASS")
              .append(" | ").append(r.violations().size())
              .append(" |\n");
        }
        for (RuleResult r : results) {
            if (!r.hasViolation()) continue;
            sb.append("\n## ").append(md(r.ruleName()))
              .append(" (").append(r.priority()).append(")\n");
            for (String v : r.violations()) {
                sb.append("- ").append(md(v)).append("\n");
            }
        }
        return sb.toString();
    }
}
