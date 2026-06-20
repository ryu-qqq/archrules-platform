package com.ryuqqq.archrules.hexagonal.fixture.violation.domain;

import java.time.LocalDateTime;

/** 도메인이 java.time.LocalDateTime에 의존 → 확장된 framework-free 위반. */
public final class LocalDateTimeDomain {
    private final LocalDateTime when = LocalDateTime.MIN;

    public LocalDateTime when() {
        return when;
    }
}
