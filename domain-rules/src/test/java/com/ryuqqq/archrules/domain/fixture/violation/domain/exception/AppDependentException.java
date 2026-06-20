package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

import com.ryuqqq.archrules.domain.fixture.violation.application.AppService;

/**
 * 규칙 #18 위반: application 레이어에 의존하는 exception.
 * 규칙 #9(DomainException 상속), #10(패키지), #14(public), #15(RuntimeException 상속) 통과.
 */
public class AppDependentException extends DomainException {

    private final AppService service;

    public AppDependentException(String message, AppService service) {
        super(message);
        this.service = service;
    }
}
