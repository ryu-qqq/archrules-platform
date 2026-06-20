package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate2;

import com.ryuqqq.archrules.domain.fixture.compliant.domain.pkg.exception.DomainException;

/**
 * ..exception.. 패키지 밖(..aggregate2..)에 위치한 DomainException 서브타입
 * — 규칙 4(DomainException 서브타입은 ..exception.. 위치) violation.
 */
public class WrongPlacedException extends DomainException {
    public WrongPlacedException(String message) {
        super(message);
    }
}
