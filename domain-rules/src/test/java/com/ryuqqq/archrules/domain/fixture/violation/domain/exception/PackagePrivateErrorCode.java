package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #4 위반: public이 아닌 ErrorCode enum (package-private).
 * 규칙 #1(ErrorCode 구현), #2(exception 패키지), #5,6,7,8 통과.
 */
enum PackagePrivateErrorCode implements ErrorCode {

    SOME_ERROR("E001", 400, "some error message");

    private final String code;
    private final int httpStatus;
    private final String message;

    PackagePrivateErrorCode(String code, int httpStatus, String message) {
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
