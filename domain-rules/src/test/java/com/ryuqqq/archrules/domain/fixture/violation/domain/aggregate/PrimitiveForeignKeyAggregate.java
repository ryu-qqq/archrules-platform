package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 11(외래키는 VO) 위반 픽스처.
 * 외래키 필드 orderId가 Long(원시타입 래퍼) 사용.
 */
public class PrimitiveForeignKeyAggregate {

    private final Long id;
    /** 규칙 11 위반: 외래키 orderId가 Long — VO여야 함 */
    private final Long orderId;
    private final Instant createdAt;
    private Instant updatedAt;

    private PrimitiveForeignKeyAggregate(Long id, Long orderId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PrimitiveForeignKeyAggregate forNew(Long orderId, Instant now) {
        return new PrimitiveForeignKeyAggregate(null, orderId, now, now);
    }

    public static PrimitiveForeignKeyAggregate of(Long id, Long orderId, Instant createdAt, Instant updatedAt) {
        return new PrimitiveForeignKeyAggregate(id, orderId, createdAt, updatedAt);
    }

    public static PrimitiveForeignKeyAggregate reconstitute(Long id, Long orderId, Instant createdAt, Instant updatedAt) {
        return new PrimitiveForeignKeyAggregate(id, orderId, createdAt, updatedAt);
    }

    public Long id() { return id; }
    public Long orderId() { return orderId; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
