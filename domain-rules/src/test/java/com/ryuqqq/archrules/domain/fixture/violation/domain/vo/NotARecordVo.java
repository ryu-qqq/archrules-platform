package com.ryuqqq.archrules.domain.fixture.violation.domain.vo;

/** record가 아님(class) → "domain VO is record"만 위반. of()는 있어 of 규칙은 통과. */
public final class NotARecordVo {
    private final long value;
    private NotARecordVo(long value) { this.value = value; }
    public static NotARecordVo of(long value) { return new NotARecordVo(value); }
    public long value() { return value; }
}
