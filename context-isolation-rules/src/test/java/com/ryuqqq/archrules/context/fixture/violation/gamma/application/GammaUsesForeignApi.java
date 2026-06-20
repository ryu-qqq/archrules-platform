package com.ryuqqq.archrules.context.fixture.violation.gamma.application;

import com.ryuqqq.archrules.context.fixture.violation.delta.api.DeltaUseCase;

public final class GammaUsesForeignApi {
    private final DeltaUseCase delta;
    public GammaUsesForeignApi(DeltaUseCase delta) { this.delta = delta; }
    public String use() { return delta.describe(); }
}
