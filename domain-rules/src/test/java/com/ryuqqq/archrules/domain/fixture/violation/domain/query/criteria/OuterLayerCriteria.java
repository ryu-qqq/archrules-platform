package com.ryuqqq.archrules.domain.fixture.violation.domain.query.criteria;

import com.ryuqqq.archrules.domain.fixture.violation.application.AppService;

/**
 * 규칙 8 위반: criteria 패키지에 있지만 ..application.. 레이어에 의존.
 * 위반 대상: "criteria does not depend on outer layers"
 * (record이고 of()도 있고 이름도 *Criteria이므로 규칙 2·3·4·10 통과)
 */
public record OuterLayerCriteria(String keyword) {
    public static OuterLayerCriteria of(String keyword) {
        return new OuterLayerCriteria(keyword);
    }

    public AppService appService() {
        return new AppService();
    }
}
