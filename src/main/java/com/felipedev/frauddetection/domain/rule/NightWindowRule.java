package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Order(3)
public class NightWindowRule implements FraudRule {

    public static final String RULE_NAME = "NIGHT_WINDOW_RESTRICTION";

    private final int startHour;
    private final int endHour;
    private final BigDecimal highAmountThreshold;

    public NightWindowRule(
            @Value("${fraud.rules.night-window.start-hour:22}") int startHour,
            @Value("${fraud.rules.night-window.end-hour:6}") int endHour,
            @Value("${fraud.rules.night-window.high-amount-threshold:1000.00}") BigDecimal highAmountThreshold) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.highAmountThreshold = highAmountThreshold;
    }

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
        LocalDateTime timestamp = request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now();
        int hour = timestamp.getHour();

        boolean isNight = (hour >= startHour || hour < endHour);
        if (!isNight) {
            return RuleExecutionResult.notTriggered(RULE_NAME);
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        if (amount.compareTo(highAmountThreshold) > 0) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    35,
                    String.format("Transação noturna (%02d:%02d) de valor elevado (R$ %s > limite de R$ %s).",
                            hour, timestamp.getMinute(), amount, highAmountThreshold)
            );
        }

        return RuleExecutionResult.triggered(
                RULE_NAME,
                15,
                String.format("Transação realizada durante a janela noturna de risco (%02d:%02d).", hour, timestamp.getMinute())
        );
    }
}