package com.ryuqqq.archrules.domain.fixture.violation.domain.query;

/**
 * 규칙 1 위반: 이름이 *Criteria이고 ..domain.. 에 있지만 ..query.criteria.. 가 아닌 ..query.. 에 위치.
 * 위반 대상: "criteria resides in criteria package"
 */
public record WrongPackageCriteria(String keyword) {
    public static WrongPackageCriteria of(String keyword) {
        return new WrongPackageCriteria(keyword);
    }
}
