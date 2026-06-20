package com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria;

/**
 * 규칙 4 위반: criteria 패키지에 record이고 이름도 *Criteria이지만 of() 정적 팩토리 없음.
 * 위반 대상: "criteria has of"
 * (record이므로 규칙 2 통과, 이름 통과, public 통과)
 */
public record NoOfCriteria(String keyword) {
    public static NoOfCriteria create(String keyword) {
        return new NoOfCriteria(keyword);
    }
}
