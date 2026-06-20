package com.ryuqqq.archrules.context.fixture.compliant.alpha.adapter;

import com.ryuqqq.archrules.context.fixture.compliant.beta.api.BetaUseCase;

public final class AlphaToBetaBridge {
    private final BetaUseCase betaUseCase;
    public AlphaToBetaBridge(BetaUseCase betaUseCase) { this.betaUseCase = betaUseCase; }
    public String bridge(String key) { return betaUseCase.describe(key); }
}
