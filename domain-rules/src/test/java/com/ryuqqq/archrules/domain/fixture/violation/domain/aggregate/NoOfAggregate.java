package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 7(of 정적팩토리) 위반 픽스처.
 * of() 메서드가 없음.
 */
public class NoOfAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    private NoOfAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NoOfAggregate forNew(Instant now) {
        return new NoOfAggregate(null, now, now);
    }

    /** 규칙 7 위반: of() 없음 — forNew / reconstitute만 있음 */
    public static NoOfAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new NoOfAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
