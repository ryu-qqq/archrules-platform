package com.ryuqqq.archrules.domain.fixture.compliant.domain.vo;

/** Enum VO — displayName() 보유 → displayName 규칙 통과. */
public enum Color {
    RED;
    public String displayName() { return name(); }
}
