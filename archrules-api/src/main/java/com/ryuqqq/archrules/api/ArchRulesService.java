package com.ryuqqq.archrules.api;

import com.tngtech.archunit.lang.ArchRule;
import java.util.Map;

/**
 * 공유 ArchUnit 규칙 묶음을 노출하는 SPI. 규칙 라이브러리가 구현하고
 * runtime이 {@link java.util.ServiceLoader}로 발견한다.
 *
 * <p>키는 규칙 이름, 값은 ArchUnit {@link ArchRule}. 모든 규칙은
 * {@code .as()} + {@code .because()} + priority를 갖춰야 한다(리포트·threshold용).
 */
@FunctionalInterface
public interface ArchRulesService {
    Map<String, ArchRule> getRules();
}
