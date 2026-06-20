package com.ryuqqq.archrules.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ryuqqq.archrules.runtime.Runner;
import com.ryuqqq.archrules.security.fixture.compliant.authhub.AuthhubClient;
import com.ryuqqq.archrules.security.fixture.compliant.gateway.GatewayAuth;
import com.ryuqqq.archrules.security.fixture.compliant.orderctx.adapter.in.publicapi.OrderResponse;
import com.ryuqqq.archrules.security.fixture.violation.marketplacectx.application.SvcCallsAuthhub;
import com.ryuqqq.archrules.security.fixture.violation.orderctx.adapter.in.publicapi.LeakyResponse;
import com.ryuqqq.archrules.security.fixture.violation.orderctx.domain.OrderAggregate;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

class SecurityBoundaryRulesTest {

    private final ArchRule authhubRule =
            new SecurityBoundaryRules().getRules().get("only gateway depends on authhub").rule();

    private final ArchRule publicAdapterRule =
            new SecurityBoundaryRules().getRules().get("public adapter exposes no domain").rule();

    @Test
    void authhubRuleIsExposed() {
        assertNotNull(authhubRule, "only gateway depends on authhub 규칙 노출");
    }

    @Test
    void publicAdapterRuleIsExposed() {
        assertNotNull(publicAdapterRule, "public adapter exposes no domain 규칙 노출");
    }

    @Test
    void gatewayDependingOnAuthhubPasses() {
        assertFalse(
                Runner.check(authhubRule, GatewayAuth.class, AuthhubClient.class).hasViolation(),
                "gateway는 authhub에 의존할 수 있다");
    }

    @Test
    void nonGatewayDependingOnAuthhubViolates() {
        assertTrue(
                Runner.check(authhubRule, SvcCallsAuthhub.class,
                        com.ryuqqq.archrules.security.fixture.violation.authhub.AuthhubClient.class).hasViolation(),
                "gateway 밖(marketplacectx.application)에서 authhub 의존은 위반");
    }

    @Test
    void cleanPublicAdapterPasses() {
        assertFalse(
                Runner.check(publicAdapterRule, OrderResponse.class).hasViolation(),
                "도메인 타입 비의존 public 어댑터는 통과");
    }

    @Test
    void publicAdapterExposingDomainViolates() {
        assertTrue(
                Runner.check(publicAdapterRule, LeakyResponse.class, OrderAggregate.class).hasViolation(),
                "public 어댑터가 도메인 타입을 노출하면 위반");
    }

    @Test
    void publicAdapterDependingOnThirdPartyDomainPackageDoesNotViolate() {
        ArchRule rule = new SecurityBoundaryRules().getRules().get("public adapter exposes no domain").rule();
        assertFalse(
            com.ryuqqq.archrules.runtime.Runner.check(rule,
                com.ryuqqq.archrules.security.fixture.compliant.orderctx.adapter.in.publicapi.ThirdPartyUsingResponse.class,
                org.thirdpartylib.domain.ThirdPartyType.class).hasViolation(),
            "서드파티 .domain 패키지(예: org.springframework.data.domain) 의존은 false-positive가 아니어야 한다");
    }
}
