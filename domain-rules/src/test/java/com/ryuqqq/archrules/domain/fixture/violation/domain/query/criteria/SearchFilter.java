package com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria;

/**
 * 규칙 3 위반: criteria 패키지에 있으나 이름이 *Criteria로 끝나지 않음.
 * 위반 대상: "criteria name ends with Criteria"
 * (record이고 of()도 있어서 규칙 2·4는 통과)
 */
public record SearchFilter(String keyword) {
    public static SearchFilter of(String keyword) {
        return new SearchFilter(keyword);
    }
}
