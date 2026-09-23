package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import com.felipedev.frauddetection.domain.model.vo.RiskScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Motor central de esteira de regras antifraude (Domain Service / Rule Engine Pipeline).
 * Encapsula a ordenação, execução em cadeia e consolidação de pontuações de risco.
 */
@Slf4j
@Component
public class FraudRuleEngine {

    private final List<FraudRule> rules;

    public FraudRuleEngine(List<FraudRule> rules) {
        List<FraudRule> sortedRules = new ArrayList<>(rules);
        AnnotationAwareOrderComparator.sort(sortedRules);
        this.rules = Collections.unmodifiableList(sortedRules);
        log.info("FraudRuleEngine inicializado com {} regras configuradas na esteira.", this.rules.size());
    }

    public List<FraudRule> getRegisteredRules() {
        return rules;
    }

    public RulePipelineResult executePipeline(TransactionAnalysisRequestDto request, AnalysisContext context) {
        List<RuleExecutionResult> ruleResults = new ArrayList<>();
        int accumulatedScore = 0;

        for (FraudRule rule : rules) {
            RuleExecutionResult result = rule.evaluate(request, context);
            ruleResults.add(result);

            if (result.isTriggered()) {
                accumulatedScore += result.getScoreContribution();
                log.debug("Regra executada e disparada: {} (+{} pontos). Motivo: {}",
                        rule.getName(), result.getScoreContribution(), result.getReason());
            }
        }

        RiskScore riskScore = RiskScore.of(accumulatedScore);
        Decision decision = riskScore.toDecision();

        return new RulePipelineResult(ruleResults, riskScore, decision);
    }

    public record RulePipelineResult(
            List<RuleExecutionResult> executedRules,
            RiskScore riskScore,
            Decision decision
    ) {}
}
