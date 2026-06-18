package com.ryuqqq.archrules.runtime;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Map;

public class FixtureRulesService implements ArchRulesService {
    @Override
    public Map<String, ArchRule> getRules() {
        ArchRule rule = noClasses().that().haveSimpleName("Banned")
                .should().bePublic()
                .as("no public Banned").because("test fixture");
        return Map.of("no public Banned", rule);
    }
}
