package com.ryuqqq.archrules.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppPackagesTest {

    @Test
    void baseOfReturnsTopTwoSegments() {
        assertEquals("com.connectly", AppPackages.baseOf("com.connectly.marketplace.order.domain"));
        assertEquals("org.hibernate", AppPackages.baseOf("org.hibernate.internal.SessionImpl"));
    }

    @Test
    void baseOfHandlesShortPackages() {
        assertEquals("com.x", AppPackages.baseOf("com.x"));
        assertEquals("flat", AppPackages.baseOf("flat"));
    }

    @Test
    void sameAppTrueWhenShareBasePrefix() {
        assertTrue(AppPackages.sameApp("com.connectly.a.domain", "com.connectly.b.domain"));
    }

    @Test
    void sameAppFalseForThirdParty() {
        // 서드파티는 앱 베이스를 공유하지 않으므로 false (false-positive 차단의 핵심)
        assertFalse(AppPackages.sameApp("com.connectly.a.domain", "org.springframework.data.domain"));
        assertFalse(AppPackages.sameApp("com.connectly.a.domain", "org.hibernate.internal"));
    }
}
