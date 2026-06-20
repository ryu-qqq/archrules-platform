package com.example.poc.compliant;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * COMPLIANT 앱 마커.
 * marketplace.product.api.ProductApi 만 공개 — order는 api를 통해서만 접근.
 * 어노테이션(@ApplicationModule) 없이 커스텀 전략으로만 구조 강제.
 */
@SpringBootApplication
public class ComplianceApp {
}
