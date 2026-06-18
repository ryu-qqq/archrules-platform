package com.ryuqqq.archrules.runtime;

import com.tngtech.archunit.lang.Priority;
import java.util.List;

public record RuleResult(String ruleName, Priority priority,
                         boolean hasViolation, List<String> violations) {}
