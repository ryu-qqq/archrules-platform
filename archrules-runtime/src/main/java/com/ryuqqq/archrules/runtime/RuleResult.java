package com.ryuqqq.archrules.runtime;

import java.util.List;

public record RuleResult(String ruleName, boolean hasViolation, List<String> violations) {}
