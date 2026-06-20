package com.ryuqqq.archrules.context.fixture.compliant.alpha.application;

import com.ryuqqq.archrules.context.fixture.compliant.alpha.domain.AlphaAggregate;

public final class AlphaService {
    public String handle(String id) { return AlphaAggregate.of(id).id(); }
}
