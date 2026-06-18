package com.ryuqqq.archrules.runtime;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;

/** 규칙 self-test 헬퍼 — 클래스 참조만으로 평가(Nebula core.Runner 본뜸). */
public final class Runner {

    private Runner() {}

    public static EvaluationResult check(ArchRule rule, Class<?>... classes) {
        JavaClasses imported = new ClassFileImporter().importClasses(classes);
        return rule.evaluate(imported);
    }
}
