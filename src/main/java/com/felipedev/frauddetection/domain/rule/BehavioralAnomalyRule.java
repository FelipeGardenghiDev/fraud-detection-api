package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Regra de Perfil Comportamental & Desvio Padrão do Cliente (Z-Score Estatístico).
 * Avalia se o valor da transação destoa criticamente do padrão de consumo habitual do cliente:
 * Z = (valor - média_histórica) / desvio_padrão.
 * Caso Z >= 3.0 (mais de 3 desvios padrão acima da média), pontua como forte anomalia comportamental.
 */
@Slf4j
@Order(6)
@Component
public class BehavioralAnomalyRule implements FraudRule {

    public static final String RULE_NAME = "BEHAVIORAL_STATISTICAL_ANOMALY";

    private final double zScoreThreshold;
    private final int minHistoryCount;

    public BehavioralAnomalyRule(
            @Value("${fraud.rules.behavioral.z-score-threshold:3.0}") double zScoreThreshold,
            @Value("${fraud.rules.behavioral.min-history-count:3}") int minHistoryCount) {
        this.zScoreThreshold = zScoreThreshold;
        this.minHistoryCount = minHistoryCount;
    }

    public BehavioralAnomalyRule() {
        this(3.0, 3);
    }

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public int getOrder() {
        return 6;
    }

    @Override
    public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
        if (context.getCustomerHistoricalCount() < minHistoryCount
                || context.getCustomerHistoricalAverage() == null
                || context.getCustomerHistoricalStdDev() == null
                || context.getCustomerHistoricalStdDev().compareTo(BigDecimal.ZERO) <= 0) {
            return RuleExecutionResult.builder()
                    .ruleName(RULE_NAME)
                    .triggered(false)
                    .scoreContribution(0)
                    .reason(String.format("Histórico insuficiente do cliente para modelo comportamental (%d/%d transações mínimas).",
                            context.getCustomerHistoricalCount(), minHistoryCount))
                    .build();
        }

        BigDecimal amount = request.getAmount();
        BigDecimal avg = context.getCustomerHistoricalAverage();
        BigDecimal stdDev = context.getCustomerHistoricalStdDev();

        // Se o valor for menor que a média, não há anomalia de valor elevado
        if (amount.compareTo(avg) <= 0) {
            return RuleExecutionResult.builder()
                    .ruleName(RULE_NAME)
                    .triggered(false)
                    .scoreContribution(0)
                    .reason("Valor transacionado condizente com a média histórica do cliente.")
                    .build();
        }

        BigDecimal diff = amount.subtract(avg);
        double zScore = diff.divide(stdDev, 4, RoundingMode.HALF_UP).doubleValue();

        if (zScore >= zScoreThreshold) {
            String reason = String.format(
                    java.util.Locale.US,
                    "Anomalia comportamental detectada: Z-Score = %.2f (valor R$ %.2f está a %.2f desvios padrão da média habitual de R$ %.2f).",
                    zScore, amount.doubleValue(), zScore, avg.doubleValue()
            );
            log.warn("Alerta de anomalia comportamental para {}: {}", request.getCustomerId(), reason);

            return RuleExecutionResult.builder()
                    .ruleName(RULE_NAME)
                    .triggered(true)
                    .scoreContribution(40)
                    .reason(reason)
                    .build();
        }

        return RuleExecutionResult.builder()
                .ruleName(RULE_NAME)
                .triggered(false)
                .scoreContribution(0)
                .reason(String.format(java.util.Locale.US, "Z-Score aceitável (%.2f < %.2f limiar).", zScore, zScoreThreshold))
                .build();
    }
}
