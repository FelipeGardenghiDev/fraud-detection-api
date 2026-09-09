package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlacklistRuleTest {

    private final BlacklistRule rule = new BlacklistRule();

    @Test
    @DisplayName("Deve atribuir score 100 imediatamente quando CPF estiver na blacklist")
    void shouldTriggerMaxScoreWhenCpfIsBlacklisted() {
        AnalysisContext context = AnalysisContext.builder()
                .cpfBlacklisted(true)
                .blacklistReason("Fraude PIX confirmada")
                .build();

        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder().build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(100, result.getScoreContribution());
    }

    @Test
    @DisplayName("Não deve disparar se cliente e dispositivo estiverem limpos")
    void shouldNotTriggerWhenClean() {
        AnalysisContext context = AnalysisContext.builder().build();
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder().build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
    }
}