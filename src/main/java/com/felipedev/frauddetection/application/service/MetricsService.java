package com.felipedev.frauddetection.application.service;

import com.felipedev.frauddetection.application.dto.MetricsResponseDto;
import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.infrastructure.persistence.repository.FraudAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final FraudAnalysisRepository repository;

    @Transactional(readOnly = true)
    public MetricsResponseDto getOverview() {
        long total = repository.count();
        long approved = repository.countByDecision(Decision.APPROVED);
        long suspicious = repository.countByDecision(Decision.SUSPICIOUS);
        long blocked = repository.countByDecision(Decision.BLOCKED);

        double blockRate = total > 0 ? ((double) blocked / total) * 100.0 : 0.0;
        BigDecimal blockedAmount = repository.sumAmountByDecision(Decision.BLOCKED);

        return MetricsResponseDto.builder()
                .totalAnalyzed(total)
                .totalApproved(approved)
                .totalSuspicious(suspicious)
                .totalBlocked(blocked)
                .blockRatePercentage(BigDecimal.valueOf(blockRate).setScale(2, RoundingMode.HALF_UP).doubleValue())
                .totalBlockedAmount(blockedAmount)
                .build();
    }
}