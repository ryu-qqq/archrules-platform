package com.example.poc.violating.marketplace.product.api;

/**
 * ProductApi — named interface (violating 앱에도 존재하지만 order는 이를 무시하고 domain을 직접 참조).
 */
public interface ProductApi {
    String getProductName(String productId);
}
