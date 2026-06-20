package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #1 위반: ErrorCode 인터페이스를 구현하지 않는 enum.
 * 나머지 규칙(#2,4,5,6,7,8)은 통과.
 */
public enum NoInterfaceErrorCode {

    SOME_ERROR("E001", 400, "some error message");

    private final String code;
    private final int httpStatus;
    private final String message;

    NoInterfaceErrorCode(String code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
