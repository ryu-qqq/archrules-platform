package com.ryuqqq.archrules.sharedkernel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuleConventionTest {

    private final Map<String, ArchRuleSpec> rules = new SharedKernelRules().getRules();

    @Test
    void everyRuleHasPriorityAndBecause() {
        assertFalse(rules.isEmpty(), "규칙이 비어있지 않다");
        rules.forEach((name, spec) -> {
            assertNotNull(spec.priority(), name + " priority non-null");
            String desc = spec.rule().getDescription();
            assertTrue(desc != null && desc.contains("because"),
                    name + " 규칙은 .because() 사유를 가진다: " + desc);
        });
    }
}
