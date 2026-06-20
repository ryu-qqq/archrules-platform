package com.ryuqqq.archrules.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ContextKeysTest {

    @Test
    void contextKeyIsPackageBeforeLayerMarker() {
        assertEquals("com.x.marketplace.product",
                ContextKeys.contextKeyOf("com.x.marketplace.product.application.service"));
        assertEquals("com.x.marketplace.order",
                ContextKeys.contextKeyOf("com.x.marketplace.order.domain.aggregate"));
    }

    @Test
    void contextKeyUsesEarliestMarker() {
        assertEquals("com.x.alpha",
                ContextKeys.contextKeyOf("com.x.alpha.application.api.dto"));
    }

    @Test
    void nonContextPackageHasNoKey() {
        assertNull(ContextKeys.contextKeyOf("com.x.shared.vo"));
        assertNull(ContextKeys.contextKeyOf("java.time"));
    }

    @Test
    void markerMustBeWholeSegment() {
        assertNull(ContextKeys.contextKeyOf("com.x.apination.stuff"));
    }

    @Test
    void layerOfReturnsLayerName() {
        assertEquals("application", ContextKeys.layerOf("com.x.alpha.application.svc"));
        assertEquals("domain", ContextKeys.layerOf("com.x.alpha.domain.agg"));
        assertEquals("api", ContextKeys.layerOf("com.x.alpha.api.port"));
        assertNull(ContextKeys.layerOf("com.x.shared.vo"));
    }

    @Test
    void markerWordsAreReserved_documentingKnownTruncation() {
        // 컨텍스트/서비스 세그먼트를 마커 단어로 이름지으면 키가 거기서 끊긴다 — 예약어 가정의 근거.
        assertEquals("com.connectly.fileflow",
                ContextKeys.contextKeyOf("com.connectly.fileflow.api.application.svc"));
        assertEquals("api",
                ContextKeys.layerOf("com.connectly.fileflow.api.application.svc"));
    }
}
