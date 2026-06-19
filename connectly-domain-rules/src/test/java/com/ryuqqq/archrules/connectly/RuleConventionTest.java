package com.ryuqqq.archrules.connectly;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.api.ArchRuleSpec;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuleConventionTest {

    private final Map<String, ArchRuleSpec> rules = new ConnectlyDomainRules().getRules();

    @Test
    void everyRuleHasPriorityAndBecause() {
        assertFalse(rules.isEmpty());
        rules.forEach((name, spec) -> {
            assertNotNull(spec.priority(), name + " priority non-null");
            String desc = spec.rule().getDescription();
            assertTrue(desc != null && desc.contains("because"), name + " has because: " + desc);
        });
    }
}
