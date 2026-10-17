package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BehavioralAnomalyRuleTest {

    private BehavioralAnomalyRule rule;

    @BeforeEach
    void setUp() {
        rule = new BehavioralAnomalyRule(3.0, 3);
    }

    @Test
    @DisplayName("Deve não disparar quando histórico de transações for insuficiente (< 3)")
    void shouldNotTriggerWhenHistoricalCountIsInsufficient() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-002")
                .amount(new BigDecimal("1500.00"))
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .customerHistoricalCount(2)
                .customerHistoricalAverage(new BigDecimal("100.00"))
                .customerHistoricalStdDev(new BigDecimal("20.00"))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
        assertTrue(result.getReason().contains("Histórico insuficiente"));
    }

    @Test
    @DisplayName("Deve não disparar quando o valor for menor ou igual à média histórica do cliente")
    void shouldNotTriggerWhenAmountIsLowerOrEqualToAverage() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-002")
                .amount(new BigDecimal("95.00"))
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .customerHistoricalCount(10)
                .customerHistoricalAverage(new BigDecimal("100.00"))
                .customerHistoricalStdDev(new BigDecimal("25.00"))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
        assertTrue(result.getReason().contains("condizente com a média histórica"));
    }

    @Test
    @DisplayName("Deve não disparar quando Z-Score for inferior a 3.0 desvios padrão")
    void shouldNotTriggerWhenZScoreIsWithinNormalLimits() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-002")
                .amount(new BigDecimal("140.00")) // Z = (140 - 100) / 25 = 1.60 < 3.0
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .customerHistoricalCount(10)
                .customerHistoricalAverage(new BigDecimal("100.00"))
                .customerHistoricalStdDev(new BigDecimal("25.00"))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
        assertTrue(result.getReason().contains("Z-Score aceitável"));
    }

    @Test
    @DisplayName("Deve disparar pontuação de anomalia (40) quando Z-Score for >= 3.0 desvios padrão")
    void shouldTriggerWhenZScoreExceedsThreshold() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-002")
                .amount(new BigDecimal("250.00")) // Z = (250 - 100) / 25 = 6.0 >= 3.0
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .customerHistoricalCount(10)
                .customerHistoricalAverage(new BigDecimal("100.00"))
                .customerHistoricalStdDev(new BigDecimal("25.00"))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(40, result.getScoreContribution());
        assertTrue(result.getReason().contains("Anomalia comportamental detectada"));
        assertTrue(result.getReason().contains("Z-Score"));
    }
}
