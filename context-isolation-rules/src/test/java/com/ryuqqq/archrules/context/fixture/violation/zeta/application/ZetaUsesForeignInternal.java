package com.ryuqqq.archrules.context.fixture.violation.zeta.application;

import com.ryuqqq.archrules.context.fixture.violation.epsilon.internal.EpsilonInternal;

public final class ZetaUsesForeignInternal {
    private final EpsilonInternal internal = new EpsilonInternal();
    public String leak() { return internal.secret(); }
}
