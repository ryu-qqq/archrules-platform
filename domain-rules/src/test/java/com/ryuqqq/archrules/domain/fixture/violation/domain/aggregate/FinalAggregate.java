package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 14(final 클래스 금지) 위반 픽스처.
 * final 클래스임.
 */
public final class FinalAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    private FinalAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FinalAggregate forNew(Instant now) {
        return new FinalAggregate(null, now, now);
    }

    public static FinalAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new FinalAggregate(id, createdAt, updatedAt);
    }

    public static FinalAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new FinalAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
