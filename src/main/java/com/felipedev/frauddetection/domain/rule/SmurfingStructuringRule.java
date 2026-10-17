package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Regra de Detecção de Fracionamento de Transações (Smurfing / Structuring).
 * Identifica a tentativa deliberada de contornar regras de alto valor ou notificações regulatórias do COAF/Bacen,
 * realizando múltiplas operações com valores imediatamente inferiores ao teto de fiscalização.
 */
@Slf4j
@Order(7)
@Component
public class SmurfingStructuringRule implements FraudRule {

    public static final String RULE_NAME = "SMURFING_STRUCTURING_DETECTION";

    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;
    private final int minRecentTxs;

    public SmurfingStructuringRule(
            @Value("${fraud.rules.smurfing.min-amount:4500.00}") BigDecimal minAmount,
            @Value("${fraud.rules.smurfing.max-amount:4999.99}") BigDecimal maxAmount,
            @Value("${fraud.rules.smurfing.min-recent-txs:2}") int minRecentTxs) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.minRecentTxs = minRecentTxs;
    }

    public SmurfingStructuringRule() {
        this(new BigDecimal("4500.00"), new BigDecimal("4999.99"), 2);
    }

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
        BigDecimal amount = request.getAmount();

        boolean isNearThreshold = amount.compareTo(minAmount) >= 0 && amount.compareTo(maxAmount) <= 0;
        boolean hasMultipleRecentTxs = context.getRecentTransactionCount() >= minRecentTxs;

        if (isNearThreshold && hasMultipleRecentTxs) {
            String reason = String.format(
                    java.util.Locale.US,
                    "Padrão suspeito de fracionamento (Smurfing/Structuring): valor R$ %.2f imediatamente abaixo do teto de R$ 5.000,00 com %d transações recentes na janela.",
                    amount.doubleValue(), context.getRecentTransactionCount()
            );
            log.warn("Alerta Smurfing para cliente {}: {}", request.getCustomerId(), reason);

            return RuleExecutionResult.builder()
                    .ruleName(RULE_NAME)
                    .triggered(true)
                    .scoreContribution(45)
                    .reason(reason)
                    .build();
        }

        return RuleExecutionResult.builder()
                .ruleName(RULE_NAME)
                .triggered(false)
                .scoreContribution(0)
                .reason("Padrão de valor não caracterizado como fracionamento deliberado de teto.")
                .build();
    }
}
