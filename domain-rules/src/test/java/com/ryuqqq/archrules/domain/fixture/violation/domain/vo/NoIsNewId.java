package com.ryuqqq.archrules.domain.fixture.violation.domain.vo;

/** Long 필드 보유 + isNew() 없음 → "connectly long id VO has isNew"만 위반(forNew는 보유). */
public record NoIsNewId(Long value) {
    public static NoIsNewId forNew() { return new NoIsNewId(null); }
}
