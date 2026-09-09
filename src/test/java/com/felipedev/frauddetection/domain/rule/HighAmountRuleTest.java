package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HighAmountRuleTest {

    private HighAmountRule rule;
    private AnalysisContext context;

    @BeforeEach
    void setUp() {
        rule = new HighAmountRule(new BigDecimal("5000.00"), new BigDecimal("20000.00"));
        context = AnalysisContext.builder().build();
    }

    @Test
    @DisplayName("Deve não disparar regra quando valor for inferior ao limite de atenção")
    void shouldNotTriggerWhenAmountIsLow() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("1500.00"))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }

    @Test
    @DisplayName("Deve disparar pontuação de atenção (30) para valores entre 5.000 e 20.000")
    void shouldTriggerSuspiciousScoreForModerateAmount() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("8500.00"))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(30, result.getScoreContribution());
    }

    @Test
    @DisplayName("Deve disparar pontuação crítica (60) para valores acima de 20.000")
    void shouldTriggerCriticalScoreForHighAmount() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("25000.00"))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(60, result.getScoreContribution());
    }
}