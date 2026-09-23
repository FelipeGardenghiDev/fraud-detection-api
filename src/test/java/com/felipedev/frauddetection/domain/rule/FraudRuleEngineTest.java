package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FraudRuleEngineTest {

    @Test
    @DisplayName("Deve executar a esteira de regras e agregar o score e a decisão corretamente")
    void shouldExecutePipelineAndAggregateScore() {
        FraudRule rule1 = new FraudRule() {
            @Override public String getName() { return "RULE_1"; }
            @Override public int getOrder() { return 1; }
            @Override public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
                return RuleExecutionResult.triggered("RULE_1", 30, "Motivo 1");
            }
        };

        FraudRule rule2 = new FraudRule() {
            @Override public String getName() { return "RULE_2"; }
            @Override public int getOrder() { return 2; }
            @Override public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
                return RuleExecutionResult.triggered("RULE_2", 20, "Motivo 2");
            }
        };

        FraudRuleEngine engine = new FraudRuleEngine(List.of(rule1, rule2));

        TransactionAnalysisRequestDto request = TransactionAnalysisRequestDto.builder()
                .amount(BigDecimal.valueOf(100))
                .build();
        AnalysisContext context = AnalysisContext.builder().build();

        FraudRuleEngine.RulePipelineResult result = engine.executePipeline(request, context);

        assertEquals(50, result.riskScore().getValue());
        assertEquals(Decision.SUSPICIOUS, result.decision());
        assertEquals(2, result.executedRules().size());
    }
}
