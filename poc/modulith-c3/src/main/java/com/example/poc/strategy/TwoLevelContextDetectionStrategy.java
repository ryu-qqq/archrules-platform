package com.example.poc.strategy;

import org.springframework.modulith.core.ApplicationModuleDetectionStrategy;
import org.springframework.modulith.core.ApplicationModuleInformation;
import org.springframework.modulith.core.JavaPackage;
import org.springframework.modulith.core.NamedInterfaces;

import java.util.stream.Stream;

/**
 * 커스텀 전략: 2단 중첩 패키지 구조를 평면 모듈 목록으로 변환.
 *
 * 목표: basePackage 아래 <svc>.<ctx> 깊이의 패키지를 각각 독립 모듈 베이스로 반환.
 *
 * 예) com.example.poc.compliant 이 basePackage일 때:
 *   com.example.poc.compliant.marketplace.product -> 모듈 베이스
 *   com.example.poc.compliant.marketplace.order   -> 모듈 베이스
 *
 * named interface: 각 모듈 내 .api 하위 패키지를 "api" named interface로 노출.
 * 어노테이션(@ApplicationModule, @NamedInterface) 0 목표.
 *
 * 등록: test/resources/application.properties 에
 *   spring.modulith.detection-strategy=com.example.poc.strategy.TwoLevelContextDetectionStrategy
 */
public class TwoLevelContextDetectionStrategy implements ApplicationModuleDetectionStrategy {

    @Override
    public Stream<JavaPackage> getModuleBasePackages(JavaPackage basePackage) {
        // basePackage의 직속 하위 패키지(svc) 각각에 대해
        // 그 하위 패키지(ctx)를 모듈 베이스로 반환 (2단 깊이 평면화)
        // 예: basePackage = com.example.poc.compliant
        //   svc = marketplace
        //   ctx = product, order  <- 이것들이 독립 모듈
        return basePackage.getDirectSubPackages()
                .stream()
                .flatMap(svcPackage -> svcPackage.getDirectSubPackages().stream());
    }

    @Override
    public NamedInterfaces detectNamedInterfaces(JavaPackage moduleBasePackage,
                                                  ApplicationModuleInformation information) {
        // .api 하위 패키지를 "api" named interface로 노출 — 어노테이션 불필요
        // NamedInterfaces.builder(JavaPackage) — JavaPackage를 인자로 받는 팩토리 메서드
        return NamedInterfaces.builder(moduleBasePackage)
                .recursive()
                .matching("api")
                .build();
    }
}
