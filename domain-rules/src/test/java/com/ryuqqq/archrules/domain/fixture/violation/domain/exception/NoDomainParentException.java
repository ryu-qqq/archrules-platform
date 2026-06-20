package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #9 위반: DomainException을 상속하지 않는 Concrete Exception.
 * RuntimeException을 직접 상속 → #15(RuntimeException 상속)는 통과, #9만 위반.
 * 이름이 "Exception"으로 끝나므로 규칙 필터에 포착됨.
 * 규칙 #10(패키지), #14(public) 통과.
 */
public class NoDomainParentException extends RuntimeException {

    public NoDomainParentException(String message) {
        super(message);
    }
}
