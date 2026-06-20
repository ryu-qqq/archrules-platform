package com.ryuqqq.archrules.domain.foo;

/**
 * BC foo의 서비스 — bar에 의존하지 않아 순환 없음.
 * 규칙 5(BC 순환 금지) compliant.
 */
public class FooService {
    public String execute() {
        return "foo";
    }
}
