package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class VelocityRule implements FraudRule {

    public static final String RULE_NAME = "VELOCITY_BURST_CHECK";

    private final int maxTransactions;
    private final int timeWindowMinutes;

    public VelocityRule(
            @Value("${fraud.rules.velocity.max-transactions:3}") int maxTransactions,
            @Value("${fraud.rules.velocity.time-window-minutes:5}") int timeWindowMinutes) {
        this.maxTransactions = maxTransactions;
        this.timeWindowMinutes = timeWindowMinutes;
    }

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
        int count = context.getRecentTransactionCount();

        if (count >= 5) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    55,
                    String.format("Velocidade crítica: %d tentativas de transação nos últimos %d minutos (possível bot/ataque).",
                            count, timeWindowMinutes)
            );
        } else if (count >= maxTransactions) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    30,
                    String.format("Frequência anormal: %d transações detectadas na janela de %d minutos.",
                            count, timeWindowMinutes)
            );
        }

        return RuleExecutionResult.notTriggered(RULE_NAME);
    }
}