package com.ryuqqq.archrules.security.fixture.violation.marketplacectx.application;

import com.ryuqqq.archrules.security.fixture.violation.authhub.AuthhubClient;

/**
 * gateway 밖(marketplacectx.application)에서 authhub에 직접 의존 — 규칙이 잡아야 함.
 * application 서비스가 authhub를 직접 호출하는 안티패턴.
 */
public class SvcCallsAuthhub {
    private final AuthhubClient authhubClient = new AuthhubClient();

    public boolean checkAuth(String token) {
        return authhubClient.authenticate(token);
    }
}
