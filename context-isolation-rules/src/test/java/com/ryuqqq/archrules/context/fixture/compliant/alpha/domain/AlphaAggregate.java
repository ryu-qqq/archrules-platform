package com.ryuqqq.archrules.context.fixture.compliant.alpha.domain;

public final class AlphaAggregate {
    private final String id;
    private AlphaAggregate(String id) { this.id = id; }
    public static AlphaAggregate of(String id) { return new AlphaAggregate(id); }
    public String id() { return id; }
}
