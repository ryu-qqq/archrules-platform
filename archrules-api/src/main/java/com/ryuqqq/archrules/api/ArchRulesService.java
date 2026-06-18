package com.ryuqqq.archrules.api;

import java.util.Map;

/**
 * 공유 ArchUnit 규칙 묶음을 노출하는 SPI. 규칙 라이브러리가 구현하고
 * runtime이 {@link java.util.ServiceLoader}로 발견한다.
 *
 * <p>키는 규칙 이름, 값은 {@link ArchRuleSpec}(ArchUnit 규칙 + priority).
 * 모든 규칙은 priority와 {@code .as()}+{@code .because()}를 갖춰야 한다.
 */
@FunctionalInterface
public interface ArchRulesService {
    Map<String, ArchRuleSpec> getRules();
}
