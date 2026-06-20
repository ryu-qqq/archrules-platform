package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

import java.time.Instant;

/**
 * 규칙 4(setter 금지) 위반 픽스처.
 * setName() public setter를 가짐.
 */
public class WithSetterAggregate {

    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;
    private String name;

    private WithSetterAggregate(Long id, Instant createdAt, Instant updatedAt, String name) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.name = name;
    }

    public static WithSetterAggregate forNew(Instant now) {
        return new WithSetterAggregate(null, now, now, "");
    }

    public static WithSetterAggregate of(Long id, Instant createdAt, Instant updatedAt, String name) {
        return new WithSetterAggregate(id, createdAt, updatedAt, name);
    }

    public static WithSetterAggregate reconstitute(Long id, Instant createdAt, Instant updatedAt, String name) {
        return new WithSetterAggregate(id, createdAt, updatedAt, name);
    }

    /** 규칙 4 위반: public setter */
    public void setName(String name) {
        this.name = name;
    }

    public Long id() { return id; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public String name() { return name; }
}
