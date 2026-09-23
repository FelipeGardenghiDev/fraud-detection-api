package com.felipedev.frauddetection.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Value Object de Domínio: TransactionAmount (DDD).
 * Encapsula quantias financeiras, precisão decimal e validações de invariantes de negócio.
 */
@Getter
@EqualsAndHashCode
public final class TransactionAmount implements Comparable<TransactionAmount> {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private final BigDecimal value;

    private TransactionAmount(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Valor da transação não pode ser nulo");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser estritamente positivo (> 0)");
        }
        this.value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static TransactionAmount of(BigDecimal value) {
        return new TransactionAmount(value);
    }

    public static TransactionAmount of(double value) {
        return new TransactionAmount(BigDecimal.valueOf(value));
    }

    public boolean isGreaterThanOrEqualTo(BigDecimal threshold) {
        return this.value.compareTo(threshold) >= 0;
    }

    public boolean isGreaterThanOrEqualTo(TransactionAmount other) {
        return this.value.compareTo(other.value) >= 0;
    }

    public String formatCurrency() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(PT_BR);
        return currencyFormat.format(this.value);
    }

    @Override
    public int compareTo(TransactionAmount other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return formatCurrency();
    }
}
