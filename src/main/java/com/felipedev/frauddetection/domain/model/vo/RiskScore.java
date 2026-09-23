package com.felipedev.frauddetection.domain.model.vo;

import com.felipedev.frauddetection.domain.model.Decision;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object de Domínio: RiskScore (DDD).
 * Encapsula o cálculo, invariantes e a decisão associada ao score de risco antifraude (0 a 100).
 */
@Getter
@EqualsAndHashCode
public final class RiskScore implements Comparable<RiskScore> {

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 100;

    private final int value;

    private RiskScore(int value) {
        this.value = Math.min(MAX_VALUE, Math.max(MIN_VALUE, value));
    }

    public static RiskScore of(int rawScore) {
        return new RiskScore(rawScore);
    }

    public boolean isApproved() {
        return value < 40;
    }

    public boolean isSuspicious() {
        return value >= 40 && value < 80;
    }

    public boolean isBlocked() {
        return value >= 80;
    }

    public Decision toDecision() {
        return Decision.fromScore(this.value);
    }

    @Override
    public int compareTo(RiskScore other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
