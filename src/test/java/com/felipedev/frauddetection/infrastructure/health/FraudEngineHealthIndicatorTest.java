package com.felipedev.frauddetection.infrastructure.health;

import com.felipedev.frauddetection.domain.rule.FraudRule;
import com.felipedev.frauddetection.domain.rule.FraudRuleEngine;
import com.felipedev.frauddetection.infrastructure.outbox.OutboxEventRepository;
import com.felipedev.frauddetection.infrastructure.outbox.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudEngineHealthIndicatorTest {

    @Test
    @DisplayName("Deve reportar UP quando todas as 7 regras estiverem ativas e fila de outbox controlada")
    void shouldReportUpWhenAllRulesConfigured() {
        FraudRuleEngine ruleEngine = mock(FraudRuleEngine.class);
        OutboxEventRepository outboxRepo = mock(OutboxEventRepository.class);

        FraudRule r1 = mock(FraudRule.class);
        when(r1.getName()).thenReturn("R1");
        List<FraudRule> rules = List.of(r1, r1, r1, r1, r1, r1, r1);

        when(ruleEngine.getRegisteredRules()).thenReturn(rules);
        when(outboxRepo.countByStatus(OutboxStatus.PENDING)).thenReturn(5L);
        when(outboxRepo.countByStatus(OutboxStatus.FAILED)).thenReturn(0L);

        FraudEngineHealthIndicator indicator = new FraudEngineHealthIndicator(ruleEngine, outboxRepo);
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("OPERATIONAL", health.getDetails().get("engineStatus"));
        assertEquals(7, health.getDetails().get("activeRulesCount"));
    }

    @Test
    @DisplayName("Deve reportar DOWN quando a esteira possuir menos de 7 regras configuradas")
    void shouldReportDownWhenRulesMissing() {
        FraudRuleEngine ruleEngine = mock(FraudRuleEngine.class);
        OutboxEventRepository outboxRepo = mock(OutboxEventRepository.class);

        when(ruleEngine.getRegisteredRules()).thenReturn(List.of(mock(FraudRule.class)));
        when(outboxRepo.countByStatus(OutboxStatus.PENDING)).thenReturn(0L);
        when(outboxRepo.countByStatus(OutboxStatus.FAILED)).thenReturn(0L);

        FraudEngineHealthIndicator indicator = new FraudEngineHealthIndicator(ruleEngine, outboxRepo);
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
