package violation.orderctx.adapter.out.persistence.entity;

/** JPA 엔티티 합성 더블 — persistence.entity 패키지에 위치 (규칙 적용 대상). */
public class OrderEntity {
    private Long id;
    private String status;

    public Long getId() { return id; }
    public String getStatus() { return status; }
}
