package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Regra de Detecção de Deslocamento Impossível (Impossible Travel / Geolocation Velocity).
 * Calcula a distância esférica entre a transação atual e a anterior usando a Fórmula de Haversine.
 * Caso a velocidade necessária para o deslocamento exceda limites físicos verossímeis (>800 km/h),
 * dispara sinalização de alta criticidade (suspeita imediata de clonagem ou comprometimento de conta).
 */
@Slf4j
@Order(5)
@Component
public class GeolocationVelocityRule implements FraudRule {

    public static final String RULE_NAME = "GEOLOCATION_IMPOSSIBLE_TRAVEL";
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final double maxSpeedKmh;
    private final double minDistanceKm;

    public GeolocationVelocityRule(
            @Value("${fraud.rules.geolocation.max-speed-kmh:800.0}") double maxSpeedKmh,
            @Value("${fraud.rules.geolocation.min-distance-km:50.0}") double minDistanceKm) {
        this.maxSpeedKmh = maxSpeedKmh;
        this.minDistanceKm = minDistanceKm;
    }

    public GeolocationVelocityRule() {
        this(800.0, 50.0);
    }

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
        if (request.getLatitude() == null || request.getLongitude() == null
                || context.getLastTransactionLatitude() == null || context.getLastTransactionLongitude() == null
                || context.getLastTransactionTime() == null) {
            return RuleExecutionResult.builder()
                    .ruleName(RULE_NAME)
                    .triggered(false)
                    .scoreContribution(0)
                    .reason("Dados geográficos insuficientes para cálculo de velocidade espacial.")
                    .build();
        }

        LocalDateTime now = request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now();
        Duration elapsed = Duration.between(context.getLastTransactionTime(), now);
        long elapsedSeconds = Math.max(1, Math.abs(elapsed.getSeconds()));
        double elapsedHours = elapsedSeconds / 3600.0;

        double distanceKm = calculateHaversineDistanceKm(
                context.getLastTransactionLatitude(),
                context.getLastTransactionLongitude(),
                request.getLatitude(),
                request.getLongitude()
        );

        double speedKmh = distanceKm / elapsedHours;

        if (distanceKm >= minDistanceKm && speedKmh > maxSpeedKmh) {
            long elapsedMinutes = Math.max(1, elapsedSeconds / 60);
            String reason = String.format(
                    java.util.Locale.US,
                    "Deslocamento geograficamente impossível detectado: %.1f km percorridos em %d min (velocidade estimada: %.0f km/h > %.0f km/h limite).",
                    distanceKm, elapsedMinutes, speedKmh, maxSpeedKmh
            );
            log.warn("Alerta de fraude: {} para o cliente {}", reason, request.getCustomerId());

            return RuleExecutionResult.builder()
                    .ruleName(RULE_NAME)
                    .triggered(true)
                    .scoreContribution(60)
                    .reason(reason)
                    .build();
        }

        return RuleExecutionResult.builder()
                .ruleName(RULE_NAME)
                .triggered(false)
                .scoreContribution(0)
                .reason(String.format(java.util.Locale.US, "Deslocamento plausível: %.1f km em %.1f h (%.0f km/h).", distanceKm, elapsedHours, speedKmh))
                .build();
    }

    /**
     * Fórmula de Haversine para cálculo de distância geodésica em esfera terrestre.
     */
    public static double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(rLat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
