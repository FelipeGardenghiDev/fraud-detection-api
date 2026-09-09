package com.felipedev.frauddetection.application.service;

import com.felipedev.frauddetection.application.dto.FraudAnalysisResponseDto;
import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.*;
import com.felipedev.frauddetection.domain.rule.FraudRule;
import com.felipedev.frauddetection.infrastructure.exception.ResourceNotFoundException;
import com.felipedev.frauddetection.infrastructure.persistence.entity.BlacklistEntity;
import com.felipedev.frauddetection.infrastructure.persistence.entity.ExecutedRuleEntity;
import com.felipedev.frauddetection.infrastructure.persistence.entity.FraudAnalysisEntity;
import com.felipedev.frauddetection.infrastructure.persistence.repository.BlacklistRepository;
import com.felipedev.frauddetection.infrastructure.persistence.repository.FraudAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudAnalysisService {

    private final List<FraudRule> rules;
    private final FraudAnalysisRepository analysisRepository;
    private final BlacklistRepository blacklistRepository;

    @Value("${fraud.rules.velocity.time-window-minutes:5}")
    private int velocityWindowMinutes;

    @Transactional
    public FraudAnalysisResponseDto analyzeTransaction(TransactionAnalysisRequestDto request) {
        log.info("Iniciando avaliação antifraude para a transação: {} do cliente: {}",
                request.getTransactionId(), request.getCustomerId());

        LocalDateTime now = LocalDateTime.now();
        if (request.getOccurredAt() == null) {
            request.setOccurredAt(now);
        }

        // 1. Constrói o contexto de enriquecimento de dados
        AnalysisContext context = buildAnalysisContext(request, now);

        // 2. Executa a esteira de regras (Rule Engine Pipeline)
        List<RuleExecutionResult> ruleResults = new ArrayList<>();
        int accumulatedScore = 0;

        for (FraudRule rule : rules) {
            RuleExecutionResult result = rule.evaluate(request, context);
            ruleResults.add(result);

            if (result.isTriggered()) {
                accumulatedScore += result.getScoreContribution();
                log.debug("Regra disparada: {} (+{} pontos). Motivo: {}",
                        rule.getName(), result.getScoreContribution(), result.getReason());
            }
        }

        // Limita o score entre 0 e 100
        int finalScore = Math.min(100, Math.max(0, accumulatedScore));
        Decision decision = Decision.fromScore(finalScore);

        // 3. Persiste a análise e auditoria no banco
        FraudAnalysisEntity entity = FraudAnalysisEntity.builder()
                .transactionId(request.getTransactionId())
                .customerId(request.getCustomerId())
                .customerCpf(request.getCustomerCpf())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .ipAddress(request.getIpAddress())
                .deviceFingerprint(request.getDeviceFingerprint())
                .location(request.getLocation())
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

        log.info("Análise concluída com sucesso. ID: {}, Score: {}, Decisão: {}",
                saved.getId(), finalScore, decision);

        return toDto(saved, ruleResults);
    }

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
        LocalDateTime windowStart = now.minusMinutes(velocityWindowMinutes);
        long recentTxCount = analysisRepository.countByCustomerIdAndAnalyzedAtAfter(request.getCustomerId(), windowStart);

        boolean cpfBlocked = blacklistRepository.existsByTypeAndValueAndActiveTrue(
                BlacklistType.CPF, request.getCustomerCpf());

        boolean ipBlocked = request.getIpAddress() != null && blacklistRepository.existsByTypeAndValueAndActiveTrue(
                BlacklistType.IP_ADDRESS, request.getIpAddress());

        boolean deviceBlocked = request.getDeviceFingerprint() != null && blacklistRepository.existsByTypeAndValueAndActiveTrue(
                BlacklistType.DEVICE_FINGERPRINT, request.getDeviceFingerprint());

        String reason = "";
        if (cpfBlocked) {
            reason = blacklistRepository.findByTypeAndValueAndActiveTrue(BlacklistType.CPF, request.getCustomerCpf())
                    .map(BlacklistEntity::getReason).orElse("CPF restrito.");
        }

        return AnalysisContext.builder()
                .recentTransactionCount((int) recentTxCount)
                .cpfBlacklisted(cpfBlocked)
                .ipBlacklisted(ipBlocked)
                .deviceBlacklisted(deviceBlocked)
                .blacklistReason(reason)
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