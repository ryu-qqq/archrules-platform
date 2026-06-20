package com.ryuqqq.archrules.domain.fixture.compliant.domain.query.criteria;

/**
 * compliant criteria — record + public + of() + 이름 *Criteria + 외부 의존 없음.
 * 6개 criteria 규칙 모두 통과.
 */
public record SearchCriteria(String keyword, int page) {
    public static SearchCriteria of(String keyword, int page) {
        return new SearchCriteria(keyword, page);
    }
}
