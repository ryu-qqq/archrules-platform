package com.ryuqqq.archrules.security.fixture.compliant.authhub;

/** authhub 인증 클라이언트 합성 더블 — gateway가 의존하는 대상(허용). */
public class AuthhubClient {
    public boolean authenticate(String token) {
        return token != null && !token.isEmpty();
    }
}
