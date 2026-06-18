package com.ryuqqq.archrules.domain.fixture.violation.domain;

import java.time.Instant;

/** 도메인이 현재 시각을 직접 읽음 — NO_TIME_IN_DOMAIN이 잡아야 함. */
public final class TimeReadingDomain {
    public Instant stamp() { return Instant.now(); }
}
