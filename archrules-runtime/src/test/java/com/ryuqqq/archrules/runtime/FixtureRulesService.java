package com.ryuqqq.archrules.runtime;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.priority;

import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.Priority;
import java.util.Map;

public class FixtureRulesService implements ArchRulesService {
    @Override
    public Map<String, ArchRule> getRules() {
        ArchRule rule = priority(Priority.HIGH)
                .noClasses().that().haveSimpleName("Banned")
                .should().bePublic()
                .as("no public Banned").because("test fixture");
        return Map.of("no public Banned", rule);
    }
}
