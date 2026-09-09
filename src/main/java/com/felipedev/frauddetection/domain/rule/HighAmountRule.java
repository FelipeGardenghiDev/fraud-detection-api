package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(2)
public class HighAmountRule implements FraudRule {

    public static final String RULE_NAME = "HIGH_AMOUNT_DETECTION";

    private final BigDecimal thresholdSuspicious;
    private final BigDecimal thresholdCritical;

    public HighAmountRule(
            @Value("${fraud.rules.high-amount.threshold-suspicious:5000.00}") BigDecimal thresholdSuspicious,
            @Value("${fraud.rules.high-amount.threshold-critical:20000.00}") BigDecimal thresholdCritical) {
        this.thresholdSuspicious = thresholdSuspicious;
        this.thresholdCritical = thresholdCritical;
    }

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
        BigDecimal amount = request.getAmount();

        if (amount == null) {
            return RuleExecutionResult.notTriggered(RULE_NAME);
        }

        if (amount.compareTo(thresholdCritical) >= 0) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    60,
                    String.format("Valor da transação (R$ %s) atinge nível crítico (>= R$ %s).", amount, thresholdCritical)
            );
        } else if (amount.compareTo(thresholdSuspicious) >= 0) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    30,
                    String.format("Valor da transação (R$ %s) excede patamar de atenção (>= R$ %s).", amount, thresholdSuspicious)
            );
        }

        return RuleExecutionResult.notTriggered(RULE_NAME);
    }
}