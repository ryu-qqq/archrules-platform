package com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria;

/**
 * 규칙 2 위반: criteria 패키지에 있으나 record가 아닌 일반 class.
 * 위반 대상: "criteria is record"
 */
public class NotRecordCriteria {
    private final String keyword;

    public NotRecordCriteria(String keyword) {
        this.keyword = keyword;
    }

    public static NotRecordCriteria of(String keyword) {
        return new NotRecordCriteria(keyword);
    }
}
