package com.ryuqqq.archrules.domain.fixture.compliant.domain;

/** 시간 직접 읽기·setter 없는 순수 도메인 (규칙 통과). */
public final class PureOrder {
    private final long id;
    public PureOrder(long id) { this.id = id; }
    public long id() { return id; }
}
