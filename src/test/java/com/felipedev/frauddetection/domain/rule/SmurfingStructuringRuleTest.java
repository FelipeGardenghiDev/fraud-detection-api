package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SmurfingStructuringRuleTest {

    private SmurfingStructuringRule rule;

    @BeforeEach
    void setUp() {
        rule = new SmurfingStructuringRule(new BigDecimal("4500.00"), new BigDecimal("4999.99"), 2);
    }

    @Test
    @DisplayName("Deve não disparar quando valor estiver fora da faixa de fracionamento (ex: R$ 1.500,00)")
    void shouldNotTriggerWhenAmountIsOutsideThreshold() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-003")
                .amount(new BigDecimal("1500.00"))
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .recentTransactionCount(5)
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }

    @Test
    @DisplayName("Deve não disparar quando valor estiver na faixa de fracionamento porém for uma única transação isolada")
    void shouldNotTriggerWhenTransactionIsIsolated() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-003")
                .amount(new BigDecimal("4850.00"))
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .recentTransactionCount(1) // Menor que o mínimo de 2 transações recentes
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }

    @Test
    @DisplayName("Deve disparar pontuação de smurfing (45) quando valor estiver entre 4.500 e 4.999,99 com múltiplas transações recentes")
    void shouldTriggerWhenStructuringPatternDetected() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-003")
                .amount(new BigDecimal("4950.00"))
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .recentTransactionCount(3)
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(45, result.getScoreContribution());
        assertTrue(result.getReason().contains("Smurfing/Structuring"));
    }

    @Test
    @DisplayName("Deve disparar nos limites de fronteira inferior (R$ 4.500,00) e superior (R$ 4.999,99)")
    void shouldTriggerOnBoundaryValues() {
        AnalysisContext context = AnalysisContext.builder().recentTransactionCount(2).build();

        TransactionAnalysisRequestDto lowerReq = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("4500.00")).build();
        assertTrue(rule.evaluate(lowerReq, context).isTriggered());

        TransactionAnalysisRequestDto upperReq = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("4999.99")).build();
        assertTrue(rule.evaluate(upperReq, context).isTriggered());
    }
}
