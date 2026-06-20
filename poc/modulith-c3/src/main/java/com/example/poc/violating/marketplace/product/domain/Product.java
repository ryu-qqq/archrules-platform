package com.example.poc.violating.marketplace.product.domain;

/**
 * Product — internal 클래스. violating 앱에서 외부 직접 참조 시 위반 감지 기대.
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
