package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 5(생성자 private) 위반 픽스처.
 * 생성자가 public임.
 */
public class PublicConstructorAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    /** 규칙 5 위반: 생성자가 public */
    public PublicConstructorAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PublicConstructorAggregate forNew(Instant now) {
        return new PublicConstructorAggregate(null, now, now);
    }

    public static PublicConstructorAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new PublicConstructorAggregate(id, createdAt, updatedAt);
    }

    public static PublicConstructorAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new PublicConstructorAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
