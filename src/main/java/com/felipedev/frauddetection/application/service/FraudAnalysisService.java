package com.felipedev.frauddetection.application.service;

import com.felipedev.frauddetection.application.dto.FraudAnalysisResponseDto;
import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.*;
import com.felipedev.frauddetection.domain.port.in.AnalyzeTransactionUseCase;
import com.felipedev.frauddetection.domain.port.out.BlacklistPort;
import com.felipedev.frauddetection.domain.port.out.FraudAnalysisRepositoryPort;
import com.felipedev.frauddetection.domain.port.out.VelocityTrackerPort;
import com.felipedev.frauddetection.domain.rule.FraudRuleEngine;
import com.felipedev.frauddetection.infrastructure.exception.ResourceNotFoundException;
import com.felipedev.frauddetection.infrastructure.messaging.FraudAlertEvent;
import com.felipedev.frauddetection.infrastructure.outbox.OutboxService;
import com.felipedev.frauddetection.infrastructure.persistence.entity.ExecutedRuleEntity;
import com.felipedev.frauddetection.infrastructure.persistence.entity.FraudAnalysisEntity;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de aplicação para análise e gestão de fraude (Primary Port Implementation).
 * Implementa {@link AnalyzeTransactionUseCase} e desacopla a infraestrutura via Outbound Ports.
 */
@Slf4j
@Service
public class FraudAnalysisService implements AnalyzeTransactionUseCase {

    private final FraudRuleEngine ruleEngine;
    private final FraudAnalysisRepositoryPort analysisRepository;
    private final BlacklistPort blacklistPort;
    private final VelocityTrackerPort velocityTracker;
    private final OutboxService outboxService;
    private final MeterRegistry meterRegistry;

    @Value("${fraud.rules.velocity.time-window-minutes:5}")
    private int velocityWindowMinutes;

    public FraudAnalysisService(
            FraudRuleEngine ruleEngine,
            FraudAnalysisRepositoryPort analysisRepository,
            BlacklistPort blacklistPort,
            VelocityTrackerPort velocityTracker,
            OutboxService outboxService,
            @Autowired(required = false) MeterRegistry meterRegistry) {
        this.ruleEngine = ruleEngine;
        this.analysisRepository = analysisRepository;
        this.blacklistPort = blacklistPort;
        this.velocityTracker = velocityTracker;
        this.outboxService = outboxService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @Transactional
    public FraudAnalysisResponseDto analyzeTransaction(TransactionAnalysisRequestDto request) {
        log.info("Iniciando avaliação antifraude para a transação: {} do cliente: {}",
                request.getTransactionId(), request.getCustomerId());

        Timer.Sample timerSample = meterRegistry != null ? Timer.start(meterRegistry) : null;

        // 1. Verificação de Idempotência: Evita reprocessamento de transações já avaliadas
        Optional<FraudAnalysisEntity> existing = analysisRepository.findByTransactionId(request.getTransactionId());
        if (existing.isPresent()) {
            log.info("Idempotency Hit: Transação {} já avaliada anteriormente. Retornando análise em cache.", request.getTransactionId());
            if (meterRegistry != null) {
                meterRegistry.counter("fraud.idempotency.hits").increment();
            }
            FraudAnalysisEntity entity = existing.get();
            List<RuleExecutionResult> rulesList = entity.getExecutedRules().stream()
                    .map(r -> RuleExecutionResult.builder()
                            .ruleName(r.getRuleName())
                            .triggered(r.isTriggered())
                            .scoreContribution(r.getScoreContribution())
                            .reason(r.getReason())
                            .build())
                    .toList();
            FraudAnalysisResponseDto dto = toDto(entity, rulesList);
            dto.setIdempotencyHit(true);
            return dto;
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getOccurredAt() == null) {
            request.setOccurredAt(now);
        }

        // 2. Constrói o contexto de enriquecimento de dados (Redis + Blacklist + Geolocalização + Z-Score)
        AnalysisContext context = buildAnalysisContext(request, now);

        // 3. Executa a esteira de regras através do motor desacoplado (Domain Service / Rule Engine Pipeline)
        FraudRuleEngine.RulePipelineResult pipelineResult = ruleEngine.executePipeline(request, context);
        int finalScore = pipelineResult.riskScore().getValue();
        Decision decision = pipelineResult.decision();
        List<RuleExecutionResult> ruleResults = pipelineResult.executedRules();

        // 4. Persiste a análise e auditoria no banco
        FraudAnalysisEntity entity = FraudAnalysisEntity.builder()
                .transactionId(request.getTransactionId())
                .customerId(request.getCustomerId())
                .customerCpf(request.getCustomerCpf())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .ipAddress(request.getIpAddress())
                .deviceFingerprint(request.getDeviceFingerprint())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .riskScore(finalScore)
                .decision(decision)
                .decisionDescription(decision.getDescription())
                .analyzedAt(now)
                .executedRules(ruleResults.stream().map(r -> ExecutedRuleEntity.builder()
                        .ruleName(r.getRuleName())
                        .triggered(r.isTriggered())
                        .scoreContribution(r.getScoreContribution())
                        .reason(r.getReason())
                        .build()).collect(Collectors.toList()))
                .build();

        FraudAnalysisEntity saved = analysisRepository.save(entity);

        // 5. Atualiza o contador de velocidade no Redis (Sliding Window Atômico)
        velocityTracker.recordTransaction(request.getCustomerId(), request.getTransactionId(), velocityWindowMinutes);

        // 6. Transactional Outbox Pattern: Grava o alerta na mesma transação atômica do banco,
        // eliminando perda de eventos caso o RabbitMQ esteja temporariamente indisponível.
        if (decision == Decision.BLOCKED || decision == Decision.SUSPICIOUS) {
            FraudAlertEvent alertEvent = FraudAlertEvent.builder()
                    .analysisId(saved.getId())
                    .transactionId(saved.getTransactionId())
                    .customerId(saved.getCustomerId())
                    .amount(saved.getAmount())
                    .paymentMethod(saved.getPaymentMethod())
                    .riskScore(finalScore)
                    .decision(decision)
                    .reason(decision.getDescription())
                    .timestamp(now)
                    .build();

            outboxService.enqueue("FRAUD_ANALYSIS", saved.getId(), "FRAUD_ALERT", alertEvent);
        }

        // 7. Telemetria e Métricas de Negócio (Micrometer / Prometheus)
        if (meterRegistry != null) {
            meterRegistry.counter("fraud.decisions.total", "decision", decision.name()).increment();
            if (decision == Decision.BLOCKED && saved.getAmount() != null) {
                meterRegistry.counter("fraud.capital.protected.amount").increment(saved.getAmount().doubleValue());
            }
            if (timerSample != null) {
                timerSample.stop(meterRegistry.timer("fraud.analysis.duration", "decision", decision.name()));
            }
        }

        log.info("Análise concluída com sucesso. ID: {}, Score: {}, Decisão: {}",
                saved.getId(), finalScore, decision);

        FraudAnalysisResponseDto responseDto = toDto(saved, ruleResults);
        responseDto.setIdempotencyHit(false);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public FraudAnalysisResponseDto getById(UUID id) {
        FraudAnalysisEntity entity = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Análise antifraude não encontrada com o ID: " + id));

        List<RuleExecutionResult> rulesList = entity.getExecutedRules().stream()
                .map(r -> RuleExecutionResult.builder()
                        .ruleName(r.getRuleName())
                        .triggered(r.isTriggered())
                        .scoreContribution(r.getScoreContribution())
                        .reason(r.getReason())
                        .build())
                    .toList();

        return toDto(entity, rulesList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudAnalysisResponseDto> getByCustomerId(String customerId) {
        return analysisRepository.findByCustomerIdOrderByAnalyzedAtDesc(customerId).stream()
                .map(entity -> {
                    List<RuleExecutionResult> rulesList = entity.getExecutedRules().stream()
                            .map(r -> RuleExecutionResult.builder()
                                    .ruleName(r.getRuleName())
                                    .triggered(r.isTriggered())
                                    .scoreContribution(r.getScoreContribution())
                                    .reason(r.getReason())
                                    .build())
                            .toList();
                    return toDto(entity, rulesList);
                })
                .toList();
    }

    private AnalysisContext buildAnalysisContext(TransactionAnalysisRequestDto request, LocalDateTime now) {
        // Busca contagem de transações recentes via Redis Sliding Window (com fallback para banco)
        int recentTxCount = velocityTracker.getRecentTransactionCount(request.getCustomerId(), velocityWindowMinutes);

        // Consulta de Restrições via Outbound Port
        boolean cpfBlocked = blacklistPort.existsActive(BlacklistType.CPF, request.getCustomerCpf());

        boolean ipBlocked = request.getIpAddress() != null && blacklistPort.existsActive(
                BlacklistType.IP_ADDRESS, request.getIpAddress());

        boolean deviceBlocked = request.getDeviceFingerprint() != null && blacklistPort.existsActive(
                BlacklistType.DEVICE_FINGERPRINT, request.getDeviceFingerprint());

        String reason = "";
        if (cpfBlocked) {
            reason = blacklistPort.findActiveReason(BlacklistType.CPF, request.getCustomerCpf())
                    .orElse("CPF restrito.");
        }

        // Histórico de Geolocalização (Impossible Travel)
        Optional<FraudAnalysisEntity> latestTx = analysisRepository.findLatestByCustomerId(request.getCustomerId());
        Double lastLat = null;
        Double lastLon = null;
        LocalDateTime lastTime = null;
        if (latestTx.isPresent()) {
            FraudAnalysisEntity prev = latestTx.get();
            lastLat = prev.getLatitude();
            lastLon = prev.getLongitude();
            lastTime = prev.getAnalyzedAt();
        }

        // Perfil Estatístico de Consumo (Média e Desvio Padrão para Z-Score)
        List<FraudAnalysisEntity> recentHistory = analysisRepository.findRecentByCustomerId(request.getCustomerId(), 20);
        int historyCount = recentHistory.size();
        BigDecimal avgAmount = null;
        BigDecimal stdDev = null;

        if (historyCount > 0) {
            BigDecimal sum = recentHistory.stream()
                    .map(FraudAnalysisEntity::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgAmount = sum.divide(BigDecimal.valueOf(historyCount), 2, RoundingMode.HALF_UP);

            if (historyCount > 1) {
                double mean = avgAmount.doubleValue();
                double sumSquaredDiffs = recentHistory.stream()
                        .map(FraudAnalysisEntity::getAmount)
                        .filter(Objects::nonNull)
                        .mapToDouble(a -> Math.pow(a.doubleValue() - mean, 2))
                        .sum();
                double variance = sumSquaredDiffs / (historyCount - 1);
                stdDev = BigDecimal.valueOf(Math.sqrt(variance)).setScale(2, RoundingMode.HALF_UP);
            } else {
                stdDev = BigDecimal.ZERO;
            }
        }

        return AnalysisContext.builder()
                .recentTransactionCount(recentTxCount)
                .cpfBlacklisted(cpfBlocked)
                .ipBlacklisted(ipBlocked)
                .deviceBlacklisted(deviceBlocked)
                .blacklistReason(reason)
                .lastTransactionLatitude(lastLat)
                .lastTransactionLongitude(lastLon)
                .lastTransactionTime(lastTime)
                .customerHistoricalAverage(avgAmount)
                .customerHistoricalStdDev(stdDev)
                .customerHistoricalCount(historyCount)
                .build();
    }

    private FraudAnalysisResponseDto toDto(FraudAnalysisEntity entity, List<RuleExecutionResult> rulesList) {
        return FraudAnalysisResponseDto.builder()
                .analysisId(entity.getId())
                .transactionId(entity.getTransactionId())
                .customerId(entity.getCustomerId())
                .riskScore(entity.getRiskScore())
                .decision(entity.getDecision())
                .decisionDescription(entity.getDecisionDescription())
                .ruleBreakdown(rulesList)
                .analyzedAt(entity.getAnalyzedAt())
                .build();
    }
}