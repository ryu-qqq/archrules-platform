package com.acme.app.domain;

import java.time.Instant;

/** 도메인이 현재 시각을 직접 읽음 — domain reads no clock(HIGH) 위반. */
public final class ClockReader {
    public Instant stamp() {
        return Instant.now();
    }
}
