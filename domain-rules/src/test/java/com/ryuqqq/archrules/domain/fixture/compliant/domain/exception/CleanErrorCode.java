package com.ryuqqq.archrules.domain.fixture.compliant.domain.exception;

/** compliant ErrorCode enum — 모든 exception 규칙(#1,2,4,5,6,7,8) 통과. */
public enum CleanErrorCode implements ErrorCode {

    SOME_ERROR("E001", 400, "some error message");

    private final String code;
    private final int httpStatus;
    private final String message;

    CleanErrorCode(String code, int httpStatus, String message) {
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
