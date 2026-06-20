package com.ryuqqq.archrules.domain.fixture.violation.domain.exception;

/**
 * package-private 픽스처 클래스들의 Class 객체를 외부에 노출하는 헬퍼.
 * PackagePrivateErrorCode / PackagePrivateException 은 package-private이라
 * 다른 패키지에서 직접 import 불가 → 여기서 Class&lt;?&gt; 를 노출한다.
 */
public final class ViolationFixtures {

    private ViolationFixtures() {}

    /** 규칙 #4 위반 픽스처 (package-private enum). */
    public static Class<?> packagePrivateErrorCodeClass() {
        return PackagePrivateErrorCode.class;
    }

    /** 규칙 #14 위반 픽스처 (package-private class). */
    public static Class<?> packagePrivateExceptionClass() {
        return PackagePrivateException.class;
    }
}
