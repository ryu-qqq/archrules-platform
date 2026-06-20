package com.example.poc.compliant.marketplace.product.api;

/**
 * ProductApi — named interface "api" 로 노출되는 공개 계약.
 * 어노테이션(@NamedInterface) 없이 패키지 경로(.api)로만 식별.
 */
public interface ProductApi {
    String getProductName(String productId);
}
