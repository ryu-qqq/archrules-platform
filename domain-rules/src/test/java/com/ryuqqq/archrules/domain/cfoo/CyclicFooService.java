package com.ryuqqq.archrules.domain.cfoo;

import com.ryuqqq.archrules.domain.cbar.CyclicBarService;

/**
 * BC cfoo — cbar에 의존(순환 의존의 한 쪽).
 * 규칙 5(BC 순환 금지) violation.
 */
public class CyclicFooService {
    private final CyclicBarService bar;

    public CyclicFooService(CyclicBarService bar) {
        this.bar = bar;
    }

    public String execute() {
        return "cfoo->" + bar.execute();
    }
}
