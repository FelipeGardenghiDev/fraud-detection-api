package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GeolocationVelocityRuleTest {

    private GeolocationVelocityRule rule;

    @BeforeEach
    void setUp() {
        rule = new GeolocationVelocityRule(800.0, 50.0);
    }

    @Test
    @DisplayName("Deve ignorar cálculo quando coordenadas geográficas estiverem ausentes")
    void shouldNotTriggerWhenCoordinatesMissing() {
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-001")
                .latitude(null)
                .longitude(null)
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .lastTransactionLatitude(-23.5505)
                .lastTransactionLongitude(-46.6333)
                .lastTransactionTime(LocalDateTime.now().minusMinutes(10))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
        assertTrue(result.getReason().contains("insuficientes"));
    }

    @Test
    @DisplayName("Deve permitir deslocamento físico plausível (ex: 20 km em 30 min)")
    void shouldNotTriggerWhenTravelSpeedIsPlausible() {
        LocalDateTime now = LocalDateTime.now();

        // São Paulo (Sé) para Guarulhos Aeroporto (~20 km em 30 min = 40 km/h)
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-001")
                .latitude(-23.4356)
                .longitude(-46.4731)
                .occurredAt(now)
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .lastTransactionLatitude(-23.5505)
                .lastTransactionLongitude(-46.6333)
                .lastTransactionTime(now.minusMinutes(30))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }

    @Test
    @DisplayName("Deve detectar e disparar pontuação crítica (60) para Impossible Travel (São Paulo -> Nova York em 15 minutos)")
    void shouldTriggerForImpossibleTravelAcrossContinents() {
        LocalDateTime now = LocalDateTime.now();

        // São Paulo: -23.5505, -46.6333
        // Nova York: 40.7128, -74.0060 (distância > 7.600 km em 15 min = ~30.000 km/h)
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-001")
                .latitude(40.7128)
                .longitude(-74.0060)
                .occurredAt(now)
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .lastTransactionLatitude(-23.5505)
                .lastTransactionLongitude(-46.6333)
                .lastTransactionTime(now.minusMinutes(15))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertTrue(result.isTriggered());
        assertEquals(60, result.getScoreContribution());
        assertTrue(result.getReason().contains("Deslocamento geograficamente impossível detectado"));
    }

    @Test
    @DisplayName("Deve ignorar velocidade alta se distância for inferior à distância mínima configurada (50 km)")
    void shouldNotTriggerWhenDistanceIsBelowMinimumThreshold() {
        LocalDateTime now = LocalDateTime.now();

        // Deslocamento de 2 km em 5 segundos (alta velocidade teórica, mas dentro do raio de tolerância/GPS drift)
        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .customerId("CUST-001")
                .latitude(-23.5520)
                .longitude(-46.6350)
                .occurredAt(now)
                .build();

        AnalysisContext context = AnalysisContext.builder()
                .lastTransactionLatitude(-23.5505)
                .lastTransactionLongitude(-46.6333)
                .lastTransactionTime(now.minusSeconds(5))
                .build();

        RuleExecutionResult result = rule.evaluate(req, context);

        assertFalse(result.isTriggered());
        assertEquals(0, result.getScoreContribution());
    }
}
