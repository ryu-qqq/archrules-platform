package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 18(updatedAt: Instant·not final) 위반 픽스처.
 * updatedAt이 final임.
 */
public class FinalUpdatedAtAggregate {

    private final Long id;
    private final Instant createdAt;
    /** 규칙 18 위반: updatedAt이 final */
    private final Instant updatedAt;

    private FinalUpdatedAtAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FinalUpdatedAtAggregate forNew(Instant now) {
        return new FinalUpdatedAtAggregate(null, now, now);
    }

    public static FinalUpdatedAtAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new FinalUpdatedAtAggregate(id, createdAt, updatedAt);
    }

    public static FinalUpdatedAtAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new FinalUpdatedAtAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
