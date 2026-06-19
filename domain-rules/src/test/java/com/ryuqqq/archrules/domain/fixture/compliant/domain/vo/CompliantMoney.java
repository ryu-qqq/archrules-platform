package com.ryuqqq.archrules.domain.fixture.compliant.domain.vo;

/** record + of() + create 없음 → VO 보편 3규칙 모두 통과. */
public record CompliantMoney(long amount) {
    public static CompliantMoney of(long amount) {
        return new CompliantMoney(amount);
    }
}
