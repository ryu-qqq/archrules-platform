package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #7 위반: getMessage() 메서드가 없는 ErrorCode enum.
 * 규칙 #1(ErrorCode 구현), #2(패키지), #4(public), #5(getCode), #6(getHttpStatus), #8(non-spring) 통과.
 */
public enum NoGetMessageErrorCode implements ErrorCode {

    SOME_ERROR("E001", 400);

    private final String code;
    private final int httpStatus;

    NoGetMessageErrorCode(String code, int httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
