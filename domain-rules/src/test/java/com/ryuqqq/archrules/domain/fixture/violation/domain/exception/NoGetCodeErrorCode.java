package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #5 위반: getCode() 메서드가 없는 ErrorCode enum.
 * 규칙 #1(ErrorCode 구현), #2(패키지), #4(public), #6,7,8 통과.
 */
public enum NoGetCodeErrorCode implements ErrorCode {

    SOME_ERROR(400, "some error message");

    private final int httpStatus;
    private final String message;

    NoGetCodeErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
