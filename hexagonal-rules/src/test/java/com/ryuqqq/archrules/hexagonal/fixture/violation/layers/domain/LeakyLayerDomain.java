package com.ryuqqq.archrules.hexagonal.fixture.violation.layers.domain;

import com.ryuqqq.archrules.hexagonal.fixture.violation.layers.adapter.out.OutboundAdapter;

/** 도메인이 adapter.out 에 의존 — 잘못된 레이어 방향(HEXAGONAL_LAYERS 위반). */
public class LeakyLayerDomain {
    private final OutboundAdapter adapter = new OutboundAdapter();
    public String use() { return adapter.send(); }
}
