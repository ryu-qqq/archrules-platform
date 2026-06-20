package com.ryuqqq.archrules.hexagonal.fixture.compliant.domain;

import java.time.Instant;

/** Instant 필드를 가진 순수 도메인 — framework-free 규칙을 통과해야 함 (aggregate Instant 의무화와 일치). */
public class InstantDomain {

    private final Instant createdAt;
    private final Instant updatedAt;

    public InstantDomain(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
