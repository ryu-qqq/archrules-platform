package com.ryuqqq.archrules.domain.fixture.compliant.domain.aggregate;

import java.time.Instant;

/**
 * Aggregate 규칙 전체 통과용 compliant 픽스처.
 * - public non-final class (final 아님)
 * - private 생성자
 * - forNew / of / reconstitute static 팩토리
 * - final Long id (자체 id — 외래키가 없어 rule#11 통과)
 * - Instant createdAt (final) / Instant updatedAt (non-final)
 */
public class PureAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    private PureAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PureAggregate forNew(Instant now) {
        return new PureAggregate(null, now, now);
    }

    public static PureAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new PureAggregate(id, createdAt, updatedAt);
    }

    public static PureAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new PureAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
