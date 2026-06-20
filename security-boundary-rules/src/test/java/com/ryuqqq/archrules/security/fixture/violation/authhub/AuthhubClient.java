package com.ryuqqq.archrules.security.fixture.violation.authhub;

/** authhub 인증 클라이언트 합성 더블 — gateway 밖에서 의존되는 대상(위반 대상). */
public class AuthhubClient {
    public boolean authenticate(String token) {
        return token != null && !token.isEmpty();
    }
}
