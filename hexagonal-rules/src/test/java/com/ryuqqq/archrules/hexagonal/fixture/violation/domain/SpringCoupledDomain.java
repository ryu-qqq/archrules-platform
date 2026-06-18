package com.ryuqqq.archrules.hexagonal.fixture.violation.domain;

import org.junit.jupiter.api.Test;

/**
 * 도메인이 프레임워크(여기선 JUnit을 프록시로)에 의존 — 규칙이 잡아야 함.
 * 실제 운영 규칙은 spring/jakarta 등을 본다(Step 4 주 참고). self-test에서는
 * 외부 의존 0 제약을 지키려 클래스패스에 이미 있는 JUnit을 위반 트리거로 쓴다.
 */
public class SpringCoupledDomain {
    public Test marker;
}
