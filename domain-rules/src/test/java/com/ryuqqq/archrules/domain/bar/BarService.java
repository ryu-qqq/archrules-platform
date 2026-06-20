package com.ryuqqq.archrules.domain.bar;

/**
 * BC bar의 서비스 — foo에 의존하지 않아 순환 없음.
 * 규칙 5(BC 순환 금지) compliant.
 */
public class BarService {
    public String execute() {
        return "bar";
    }
}
