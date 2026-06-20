package com.ryuqqq.archrules.domain.fixture.violation.domain.aggregate;

/**
 * package-private PackagePrivateAggregate의 Class 객체를 외부에 노출하는 헬퍼.
 * PackagePrivateAggregate 는 package-private이라 다른 패키지에서 직접 .class 참조 불가
 * → 여기서 Class&lt;?&gt; 를 노출한다.
 */
public final class PackagePrivateAggregateAccess {

    private PackagePrivateAggregateAccess() {}

    /** 규칙 13 위반 픽스처 (package-private aggregate). */
    public static Class<?> target() {
        return PackagePrivateAggregate.class;
    }
}
