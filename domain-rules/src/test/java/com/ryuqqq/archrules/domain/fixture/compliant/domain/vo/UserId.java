package com.ryuqqq.archrules.domain.fixture.compliant.domain.vo;

/** Long ID VO — forNew()/isNew() 보유 → forNew·isNew 규칙 통과. */
public record UserId(Long value) {
    public static UserId of(Long value) { return new UserId(value); }
    public static UserId forNew() { return new UserId(null); }
    public boolean isNew() { return value == null; }
}
