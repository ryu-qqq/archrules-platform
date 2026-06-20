package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 13(public 클래스) 위반 픽스처.
 * package-private 클래스임.
 */
class PackagePrivateAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    private PackagePrivateAggregate(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    static PackagePrivateAggregate forNew(Instant now) {
        return new PackagePrivateAggregate(null, now, now);
    }

    static PackagePrivateAggregate of(Long id, Instant createdAt, Instant updatedAt) {
        return new PackagePrivateAggregate(id, createdAt, updatedAt);
    }

    static PackagePrivateAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt) {
        return new PackagePrivateAggregate(id, createdAt, updatedAt);
    }

    Long id() { return id; }
    Instant createdAt() { return createdAt; }
    Instant updatedAt() { return updatedAt; }
}
