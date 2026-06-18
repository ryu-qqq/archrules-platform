package com.ryuqqq.archrules.api;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Objects;

/**
 * 노출 규칙 1건 = ArchUnit {@link ArchRule} + {@link Priority}.
 * priority는 runtime이 ArchRule에서 읽을 수 없으므로(ArchUnit 1.2.1 미노출) 여기 싣는다.
 */
public record ArchRuleSpec(ArchRule rule, Priority priority) {
    public ArchRuleSpec {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(priority, "priority");
    }
}
