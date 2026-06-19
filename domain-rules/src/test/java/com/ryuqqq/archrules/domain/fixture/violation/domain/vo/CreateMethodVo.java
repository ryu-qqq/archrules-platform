package com.ryuqqq.archrules.domain.fixture.violation.domain.vo;

/** record + of() 이지만 create* 메서드 보유 → "domain VO has no create method"만 위반. */
public record CreateMethodVo(long amount) {
    public static CreateMethodVo of(long amount) { return new CreateMethodVo(amount); }
    public static CreateMethodVo createDefault() { return new CreateMethodVo(0L); }
}
