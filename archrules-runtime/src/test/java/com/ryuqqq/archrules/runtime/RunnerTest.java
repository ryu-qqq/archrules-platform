package com.ryuqqq.archrules.runtime;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

class RunnerTest {

    static class CleanClass {
        int value() { return 1; }
    }

    @Test
    void passingRuleHasNoViolation() {
        ArchRule rule = noClasses().should().accessField(System.class, "out")
                .as("no System.out").because("test");

        EvaluationResult result = Runner.check(rule, CleanClass.class);

        assertFalse(result.hasViolation());
    }

    @Test
    void failingRuleReportsViolation() {
        ArchRule rule = noClasses().should().haveSimpleName("CleanClass")
                .as("no CleanClass").because("test");

        EvaluationResult result = Runner.check(rule, CleanClass.class);

        assertTrue(result.hasViolation());
    }
}
