package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VelocityRuleTest {

    private final VelocityRule rule = new VelocityRule(3, 5);

    @Test
    @DisplayName("Deve disparar alerta crítico quando houver 5 ou mais tentativas na janela de tempo")
    void shouldTriggerCriticalWhenBurstTransactions() {
        AnalysisContext context = AnalysisContext.builder()
                .recentTransactionCount(6)
                .build();

        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder().build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(55, result.getScoreContribution());
    }
}