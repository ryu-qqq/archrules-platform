package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 17(createdAt: Instant·final) 위반 픽스처.
 * createdAt이 non-final임.
 */
public class NonFinalCreatedAtAggregate {

    private final Long id;
    /** 규칙 17 위반: createdAt이 non-final */
    private Instant createdAt;
    private Instant updatedAt;

    private NonFinalCreatedAtAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NonFinalCreatedAtAggregate forNew(Instant now) {
        return new NonFinalCreatedAtAggregate(null, now, now);
    }

    public static NonFinalCreatedAtAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new NonFinalCreatedAtAggregate(id, createdAt, updatedAt);
    }

    public static NonFinalCreatedAtAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new NonFinalCreatedAtAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
