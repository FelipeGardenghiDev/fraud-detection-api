package com.felipedev.frauddetection.infrastructure.health;

import com.felipedev.frauddetection.domain.rule.FraudRule;
import com.felipedev.frauddetection.domain.rule.FraudRuleEngine;
import com.felipedev.frauddetection.infrastructure.outbox.OutboxEventRepository;
import com.felipedev.frauddetection.infrastructure.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Health Indicator customizado para o Actuator (/actuator/health).
 * Monitora a integridade da esteira de regras e o acúmulo de eventos no Outbox.
 */
@Component
@RequiredArgsConstructor
public class FraudEngineHealthIndicator implements HealthIndicator {

    private final FraudRuleEngine ruleEngine;
    private final OutboxEventRepository outboxEventRepository;

    @Override
    public Health health() {
        List<FraudRule> rules = ruleEngine.getRegisteredRules();
        int ruleCount = rules.size();
        long pendingOutboxCount = outboxEventRepository.countByStatus(OutboxStatus.PENDING);
        long failedOutboxCount = outboxEventRepository.countByStatus(OutboxStatus.FAILED);

        if (ruleCount < 7) {
            return Health.down()
                    .withDetail("reason", "Esteira antifraude incompleta: esperado 7 regras, configuradas " + ruleCount)
                    .withDetail("configuredRules", ruleCount)
                    .build();
        }

        Health.Builder builder = failedOutboxCount > 50 ? Health.down() : Health.up();

        return builder
                .withDetail("engineStatus", "OPERATIONAL")
                .withDetail("activeRulesCount", ruleCount)
                .withDetail("rulesPipeline", rules.stream().map(FraudRule::getName).toList())
                .withDetail("outboxPendingEvents", pendingOutboxCount)
                .withDetail("outboxFailedEvents", failedOutboxCount)
                .build();
    }
}
