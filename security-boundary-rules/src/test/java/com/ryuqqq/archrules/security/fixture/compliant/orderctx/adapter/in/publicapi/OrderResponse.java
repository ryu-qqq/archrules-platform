package com.ryuqqq.archrules.security.fixture.compliant.orderctx.adapter.in.publicapi;

/**
 * public 입력 어댑터 DTO — 도메인 타입 의존 없음 (규칙 통과해야 함).
 * 순수 데이터 전달 객체로, 도메인 타입을 노출하지 않는다.
 */
public record OrderResponse(String orderId, String status, long totalAmount) {}
