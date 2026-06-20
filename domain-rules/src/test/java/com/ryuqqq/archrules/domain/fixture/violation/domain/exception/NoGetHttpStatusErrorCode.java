package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * 규칙 #6 위반: getHttpStatus() 메서드가 없는 ErrorCode enum.
 * 규칙 #1(ErrorCode 구현), #2(패키지), #4(public), #5(getCode), #7(getMessage) 통과.
 * 규칙 #8도 getHttpStatus 없으면 조건 미충족 → 위반이나 이 픽스처는 #6 전용.
 */
public enum NoGetHttpStatusErrorCode implements ErrorCode {

    SOME_ERROR("E001", "some error message");

    private final String code;
    private final String message;

    NoGetHttpStatusErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
