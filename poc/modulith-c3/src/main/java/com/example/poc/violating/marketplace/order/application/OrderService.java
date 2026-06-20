package com.example.poc.violating.marketplace.order.application;

import com.example.poc.violating.marketplace.product.domain.Product;

/**
 * OrderService — VIOLATION: product.domain.Product 를 직접 참조.
 * api 대신 internal 클래스를 직접 의존 → verify() 가 위반을 잡아야 함.
 */
public class OrderService {

    public String describeOrder(String productId) {
        // internal 클래스를 직접 생성 — Spring Modulith가 이걸 잡아야 함
        Product product = new Product(productId, "Test Product");
        return "Order for: " + product.getName();
    }
}
