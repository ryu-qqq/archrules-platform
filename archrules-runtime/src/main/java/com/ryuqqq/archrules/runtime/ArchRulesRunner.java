package com.ryuqqq.archrules.runtime;

import com.ryuqqq.archrules.api.ArchRulesService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/** 클래스패스의 모든 {@link ArchRulesService}를 발견해 규칙을 평가한다. */
public final class ArchRulesRunner {

    private ArchRulesRunner() {}

    public static List<RuleResult> run(Path classesDir) {
        JavaClasses classes = new ClassFileImporter().importPath(classesDir);
        return run(classes);
    }

    public static List<RuleResult> run(JavaClasses classes) {
        List<RuleResult> results = new ArrayList<>();
        for (ArchRulesService service : ServiceLoader.load(ArchRulesService.class)) {
            for (Map.Entry<String, ArchRule> entry : service.getRules().entrySet()) {
                EvaluationResult eval = entry.getValue().evaluate(classes);
                List<String> messages = eval.getFailureReport().getDetails();
                results.add(new RuleResult(entry.getKey(), eval.hasViolation(), List.copyOf(messages)));
            }
        }
        return results;
    }
}
