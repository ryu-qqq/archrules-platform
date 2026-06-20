package com.ryuqqq.archrules.context.fixture.compliant.theta.application;

import org.fakelib.api.FakeApi;

public final class ThetaCoreUsesThirdPartyApi {
    private final FakeApi api;
    public ThetaCoreUsesThirdPartyApi(FakeApi api) { this.api = api; }
    public String use() { return api.describe(); }
}
