package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 8(reconstitute 정적팩토리) 위반 픽스처.
 * reconstitute() 메서드가 없음.
 */
public class NoReconstituteAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    private NoReconstituteAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NoReconstituteAggregate forNew(Instant now) {
        return new NoReconstituteAggregate(null, now, now);
    }

    public static NoReconstituteAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new NoReconstituteAggregate(id, createdAt, updatedAt);
    }

    /** 규칙 8 위반: reconstitute() 없음 — forNew / of만 있음 */

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
