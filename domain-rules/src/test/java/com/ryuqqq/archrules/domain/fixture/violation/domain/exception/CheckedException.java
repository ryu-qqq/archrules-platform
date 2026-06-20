package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #15 위반: RuntimeException을 상속하지 않는 Checked Exception.
 * Exception을 직접 상속하면 RuntimeException 상속 규칙 위반.
 * haveSimpleNameEndingWith("Exception") 필터에 해당.
 * doNotHaveSimpleName("DomainException") 필터에 해당 안 함(이름이 다름).
 * 규칙 #9(DomainException 상속)도 위반하나 이 픽스처는 #15 전용으로 사용.
 */
public class CheckedException extends Exception {

    public CheckedException(String message) {
        super(message);
    }
}
