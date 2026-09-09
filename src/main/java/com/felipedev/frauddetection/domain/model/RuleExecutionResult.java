package com.felipedev.frauddetection.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResult {
    private String ruleName;
    private boolean triggered;
    private int scoreContribution;
    private String reason;

    public static RuleExecutionResult notTriggered(String ruleName) {
        return RuleExecutionResult.builder()
                .ruleName(ruleName)
                .triggered(false)
                .scoreContribution(0)
                .reason("Regra não violada.")
                .build();
    }

    public static RuleExecutionResult triggered(String ruleName, int score, String reason) {
        return RuleExecutionResult.builder()
                .ruleName(ruleName)
                .triggered(true)
                .scoreContribution(score)
                .reason(reason)
                .build();
    }
}