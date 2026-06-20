package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 6(forNew 정적팩토리) 위반 픽스처.
 * forNew() 메서드가 없음.
 */
public class NoForNewAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    private NoForNewAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 규칙 6 위반: forNew() 없음 — of / reconstitute만 있음 */
    public static NoForNewAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new NoForNewAggregate(id, createdAt, updatedAt);
    }

    public static NoForNewAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new NoForNewAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
