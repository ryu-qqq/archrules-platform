package com.ryuqqq.archrules.connectly.fixture.violation.domain.vo;

/** Id로 끝나지만 forNew() 없음 → "connectly id VO has forNew"만 위반(isNew는 보유). */
public record NoForNewId(Long value) {
    public boolean isNew() { return value == null; }
}
