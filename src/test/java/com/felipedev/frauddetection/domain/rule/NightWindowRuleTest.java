package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NightWindowRuleTest {

    private NightWindowRule rule;
    private AnalysisContext context;

    @BeforeEach
    void setUp() {
        rule = new NightWindowRule(22, 6, new BigDecimal("1000.00"));
        context = AnalysisContext.builder().build();
    }

    @Test
    @DisplayName("Deve não disparar regra para transações diurnas")
    void shouldNotTriggerDuringDaytime() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("5000.00"))
                .occurredAt(LocalDateTime.of(2026, 9, 9, 14, 30))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
    }

    @Test
    @DisplayName("Deve pontuar 35 para transações noturnas de alto valor")
    void shouldTriggerHighRiskForNightHighAmount() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("3500.00"))
                .occurredAt(LocalDateTime.of(2026, 9, 9, 23, 15))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(35, result.getScoreContribution());
    }
}