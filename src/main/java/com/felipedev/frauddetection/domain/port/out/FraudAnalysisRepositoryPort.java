package com.felipedev.frauddetection.domain.port.out;

import com.felipedev.frauddetection.infrastructure.persistence.entity.FraudAnalysisEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída (Outbound Port) para persistência e recuperação de análises antifraude.
 */
public interface FraudAnalysisRepositoryPort {

    FraudAnalysisEntity save(FraudAnalysisEntity entity);

    Optional<FraudAnalysisEntity> findById(UUID id);

    Optional<FraudAnalysisEntity> findByTransactionId(String transactionId);

    List<FraudAnalysisEntity> findByCustomerIdOrderByAnalyzedAtDesc(String customerId);

    long countRecentByCustomerId(String customerId, LocalDateTime since);

    Optional<FraudAnalysisEntity> findLatestByCustomerId(String customerId);

    List<FraudAnalysisEntity> findRecentByCustomerId(String customerId, int limit);
}
