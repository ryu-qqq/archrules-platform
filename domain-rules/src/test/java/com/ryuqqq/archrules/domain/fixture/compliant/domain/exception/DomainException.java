package com.ryuqqq.archrules.domain.fixture.compliant.domain.exception;

/** stub — 외부 의존 없이 이름 기반 비교로 self-test 가능하게 하는 기반 예외 클래스. */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
