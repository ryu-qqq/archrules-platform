package com.ryuqqq.archrules.security.fixture.compliant.orderctx.adapter.in.publicapi;

import org.thirdpartylib.domain.ThirdPartyType;

public final class ThirdPartyUsingResponse {
    private final ThirdPartyType lib = new ThirdPartyType();
    public String use() { return lib.value(); }
}
