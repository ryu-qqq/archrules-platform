package com.example.poc.compliant.marketplace.order.application;

import com.example.poc.compliant.marketplace.product.api.ProductApi;

/**
 * OrderService — product 모듈의 named interface(.api)만 의존.
 * COMPLIANT: product.api.ProductApi 를 통해서만 접근 → 규칙 통과 기대.
 */
public class OrderService {

    private final ProductApi productApi;

    public OrderService(ProductApi productApi) {
        this.productApi = productApi;
    }

    public String describeOrder(String productId) {
        return "Order for: " + productApi.getProductName(productId);
    }
}
