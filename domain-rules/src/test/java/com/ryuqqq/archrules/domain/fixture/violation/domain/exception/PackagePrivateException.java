package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #14 위반: public이 아닌 Concrete Exception (package-private).
 * 규칙 #9(DomainException 상속), #10(패키지), #15(RuntimeException 상속) 통과.
 */
class PackagePrivateException extends DomainException {

    PackagePrivateException(String message) {
        super(message);
    }
}
