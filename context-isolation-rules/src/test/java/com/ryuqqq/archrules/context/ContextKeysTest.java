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
}
