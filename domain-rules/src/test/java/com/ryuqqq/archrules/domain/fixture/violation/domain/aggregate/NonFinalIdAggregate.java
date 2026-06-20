package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 9(id 필드 final) 위반 픽스처.
 * id 필드가 final이 아님.
 */
public class NonFinalIdAggregate {

    /** 규칙 9 위반: id가 non-final */
    private Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    private NonFinalIdAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NonFinalIdAggregate forNew(Instant now) {
        return new NonFinalIdAggregate(null, now, now);
    }

    public static NonFinalIdAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new NonFinalIdAggregate(id, createdAt, updatedAt);
    }

    public static NonFinalIdAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new NonFinalIdAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
