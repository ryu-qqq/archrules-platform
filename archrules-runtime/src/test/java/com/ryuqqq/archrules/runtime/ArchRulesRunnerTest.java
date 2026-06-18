package com.ryuqqq.archrules.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchRulesRunnerTest {

    public static class Banned {}

    @Test
    void discoversServiceAndReportsViolation() {
        JavaClasses classes = new ClassFileImporter()
                .importClasses(Banned.class);

        List<RuleResult> results = ArchRulesRunner.run(classes);

        assertEquals(1, results.size());
        RuleResult r = results.get(0);
        assertEquals("no public Banned", r.ruleName());
        assertEquals(com.tngtech.archunit.lang.Priority.HIGH, r.priority());
        assertTrue(r.hasViolation());
        assertTrue(r.violations().size() >= 1);
    }
}
