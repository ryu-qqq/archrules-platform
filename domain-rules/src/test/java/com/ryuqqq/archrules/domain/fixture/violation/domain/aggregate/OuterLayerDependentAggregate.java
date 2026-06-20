package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate.application.ApplicationService;
import java.time.Instant;

/**
 * 규칙 16(외부 레이어 의존 금지) 위반 픽스처.
 * application 패키지의 ApplicationService에 의존함.
 */
public class OuterLayerDependentAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;
    /** 규칙 16 위반: application 패키지 클래스에 의존 */
    private final ApplicationService service;

    private OuterLayerDependentAggregate(Long id, Instant createdAt, Instant updatedAt, ApplicationService service) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.service = service;
    }

    public static OuterLayerDependentAggregate forNew(Instant now, ApplicationService service) {
        return new OuterLayerDependentAggregate(null, now, now, service);
    }

    public static OuterLayerDependentAggregate of(Long id, Instant createdAt, Instant updatedAt, ApplicationService service) {
        return new OuterLayerDependentAggregate(id, createdAt, updatedAt, service);
    }

    public static OuterLayerDependentAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt, ApplicationService service) {
        return new OuterLayerDependentAggregate(id, createdAt, updatedAt, service);
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
