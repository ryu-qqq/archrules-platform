package com.ryuqqq.archrules.context.fixture.violation.alpha.application;

import com.ryuqqq.archrules.context.fixture.violation.beta.domain.BetaAggregate;

public final class LeakyAlphaService {
    private final BetaAggregate beta = new BetaAggregate();
    public String leak() { return beta.value(); }
}
