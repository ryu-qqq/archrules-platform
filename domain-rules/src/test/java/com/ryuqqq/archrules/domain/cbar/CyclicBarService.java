package com.ryuqqq.archrules.domain.cbar;

import com.ryuqqq.archrules.domain.cfoo.CyclicFooService;

/**
 * BC cbar — cfoo에 의존(순환 의존의 다른 한 쪽).
 * 규칙 5(BC 순환 금지) violation.
 */
public class CyclicBarService {
    private final CyclicFooService foo;

    public CyclicBarService(CyclicFooService foo) {
        this.foo = foo;
    }

    public String execute() {
        return "cbar->" + foo.execute();
    }
}
