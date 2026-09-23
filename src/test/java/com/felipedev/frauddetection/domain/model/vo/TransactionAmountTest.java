package com.felipedev.frauddetection.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionAmountTest {

    @Test
    @DisplayName("Deve formatar e comparar montantes financeiros com precisão")
    void shouldFormatAndCompareAmount() {
        TransactionAmount amount = TransactionAmount.of(1500.50);

        assertEquals(new BigDecimal("1500.50"), amount.getValue());
        assertTrue(amount.isGreaterThanOrEqualTo(new BigDecimal("1000.00")));
        assertFalse(amount.isGreaterThanOrEqualTo(new BigDecimal("2000.00")));
        assertTrue(amount.formatCurrency().contains("1.500,50"));
    }

    @Test
    @DisplayName("Deve rejeitar valor nulo ou negativo")
    void shouldRejectInvalidAmounts() {
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of(null));
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of(0.0));
        assertThrows(IllegalArgumentException.class, () -> TransactionAmount.of(-50.0));
    }
}
