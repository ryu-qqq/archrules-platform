package com.ryuqqq.archrules.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchRulesServiceContractTest {

    @Test
    void implementationReturnsNamedSpecsWithPriority() {
        ArchRulesService service = () -> {
            ArchRule rule = classes().should().bePublic().as("all public").because("test");
            return Map.of("all public", new ArchRuleSpec(rule, Priority.MEDIUM));
        };

        Map<String, ArchRuleSpec> rules = service.getRules();

        assertNotNull(rules);
        assertEquals(1, rules.size());
        ArchRuleSpec spec = rules.get("all public");
        assertNotNull(spec);
        assertNotNull(spec.rule());
        assertEquals(Priority.MEDIUM, spec.priority());
    }
}
