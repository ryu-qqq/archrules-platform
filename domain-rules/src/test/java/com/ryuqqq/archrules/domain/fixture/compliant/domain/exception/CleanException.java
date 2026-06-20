package com.ryuqqq.archrules.domain.fixture.compliant.domain.exception;

/** compliant Concrete Exception — 모든 exception 규칙(#9,10,14,15,18) 통과. */
public class CleanException extends DomainException {

    public CleanException(String message) {
        super(message);
    }

    public CleanException(String message, Throwable cause) {
        super(message, cause);
    }
}
