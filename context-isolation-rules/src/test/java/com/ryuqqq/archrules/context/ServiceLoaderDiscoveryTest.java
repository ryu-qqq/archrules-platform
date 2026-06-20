package com.ryuqqq.archrules.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.api.ArchRulesService;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

class ServiceLoaderDiscoveryTest {

    @Test
    void isDiscoverableViaServiceLoader() {
        boolean found = StreamSupport.stream(
                        ServiceLoader.load(ArchRulesService.class).spliterator(), false)
                .anyMatch(s -> s instanceof ContextIsolationRules);
        assertTrue(found, "ContextIsolationRules 가 ServiceLoader로 발견되어야 한다");
    }
}
