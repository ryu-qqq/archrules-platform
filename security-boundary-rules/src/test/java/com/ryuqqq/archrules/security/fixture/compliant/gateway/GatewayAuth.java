package com.ryuqqq.archrules.security.fixture.compliant.gateway;

import com.ryuqqq.archrules.security.fixture.compliant.authhub.AuthhubClient;

/**
 * gateway 패키지 — authhub 의존 허용 (규칙 통과해야 함).
 * gateway는 엣지이므로 authhub를 의존할 수 있다.
 */
public class GatewayAuth {
    private final AuthhubClient authhubClient = new AuthhubClient();

    public boolean verify(String token) {
        return authhubClient.authenticate(token);
    }
}
