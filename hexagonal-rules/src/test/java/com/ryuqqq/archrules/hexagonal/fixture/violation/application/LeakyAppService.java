package com.ryuqqq.archrules.hexagonal.fixture.violation.application;

import org.junit.jupiter.api.Test;

/**
 * application이 프레임워크(여기선 org.junit 프록시)에 직접 의존 — 규칙이 잡아야 함.
 * 운영 규칙은 org.springframework.web/jakarta.servlet/jakarta.persistence 등을 본다.
 */
public class LeakyAppService {
    public Test marker;
}
