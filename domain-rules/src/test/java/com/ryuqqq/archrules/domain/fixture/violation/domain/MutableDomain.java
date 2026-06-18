package com.ryuqqq.archrules.domain.fixture.violation.domain;

/** setter 보유 — NO_SETTERS_IN_DOMAIN이 잡아야 함. */
public final class MutableDomain {
    private String name;
    public void setName(String name) { this.name = name; }
    public String name() { return name; }
}
