package com.ryuqqq.archrules.hexagonal.fixture.compliant.layers.adapter.out;

import com.ryuqqq.archrules.hexagonal.fixture.compliant.layers.domain.CleanLayerDomain;

/** adapter.out → domain (올바른 방향). */
public class CleanAdapter {
    private CleanLayerDomain domain;
    public CleanLayerDomain domain() { return domain; }
}
