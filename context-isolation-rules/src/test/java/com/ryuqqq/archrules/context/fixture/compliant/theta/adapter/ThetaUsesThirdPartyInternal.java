package com.ryuqqq.archrules.context.fixture.compliant.theta.adapter;

import org.fakelib.internal.FakeInternal;

public final class ThetaUsesThirdPartyInternal {
    private final FakeInternal lib = new FakeInternal();
    public String use() { return lib.secret(); }
}
