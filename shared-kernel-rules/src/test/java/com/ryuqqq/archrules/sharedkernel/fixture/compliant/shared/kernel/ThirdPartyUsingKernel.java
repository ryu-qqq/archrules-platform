package com.ryuqqq.archrules.sharedkernel.fixture.compliant.shared.kernel;

import org.thirdpartylib.domain.ThirdPartyType;

public final class ThirdPartyUsingKernel {
    private final ThirdPartyType lib = new ThirdPartyType();
    public String use() { return lib.value(); }
}
