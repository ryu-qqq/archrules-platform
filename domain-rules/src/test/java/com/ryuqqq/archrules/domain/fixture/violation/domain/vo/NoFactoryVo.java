package com.ryuqqq.archrules.domain.fixture.violation.domain.vo;

/** record지만 of() 없음 → "domain VO has static factory of"만 위반. */
public record NoFactoryVo(long amount) {}
