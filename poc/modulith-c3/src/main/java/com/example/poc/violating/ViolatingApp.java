package com.example.poc.violating;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VIOLATING 앱 마커.
 * order.application.OrderService 가 product.domain.Product 를 직접 참조 — internal 위반.
 * 검증 시 예외 발생 기대.
 */
@SpringBootApplication
public class ViolatingApp {
}
