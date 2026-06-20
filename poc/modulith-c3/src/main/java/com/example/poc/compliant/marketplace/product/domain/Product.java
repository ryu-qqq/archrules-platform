package com.example.poc.compliant.marketplace.product.domain;

/**
 * Product 도메인 클래스 — internal (product 모듈 외부 접근 금지).
 * named interface가 api 패키지만이므로 이 클래스는 다른 모듈에서 접근 불가.
 */
public class Product {
    private final String id;
    private final String name;

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
