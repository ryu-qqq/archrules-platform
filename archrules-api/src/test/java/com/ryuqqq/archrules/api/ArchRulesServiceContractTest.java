package com.ryuqqq.archrules.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.tngtech.archunit.lang.ArchRule;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchRulesServiceContractTest {

    @Test
    void implementationReturnsNamedRules() {
        ArchRulesService service = () -> {
            ArchRule rule = classes().should().bePublic().as("all public").because("test");
            return Map.of("all public", rule);
        };

        Map<String, ArchRule> rules = service.getRules();

        assertNotNull(rules);
        assertEquals(1, rules.size());
        assertNotNull(rules.get("all public"));
    }
}
