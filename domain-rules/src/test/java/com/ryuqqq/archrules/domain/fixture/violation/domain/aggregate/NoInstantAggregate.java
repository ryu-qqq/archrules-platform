package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

/**
 * 규칙 10(Instant 필드 최소 1개) 위반 픽스처.
 * Instant 타입 필드가 없음.
 */
public class NoInstantAggregate {

    private final Long id;
    /** 규칙 10 위반: Instant 필드 없음 — String으로 대체 */
    private final String createdAt;
    private String updatedAt;

    private NoInstantAggregate(Long id, String createdAt, String updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NoInstantAggregate forNew(String now) {
        return new NoInstantAggregate(null, now, now);
    }

    public static NoInstantAggregate of(Long id, String createdAt, String updatedAt) {
        return new NoInstantAggregate(id, createdAt, updatedAt);
    }

    public static NoInstantAggregate reconstitute(Long id, String createdAt, String updatedAt) {
        return new NoInstantAggregate(id, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public String createdAt() { return createdAt; }
    public String updatedAt() { return updatedAt; }
}
