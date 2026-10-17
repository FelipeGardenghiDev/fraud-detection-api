package com.felipedev.frauddetection.infrastructure.persistence.adapter;

import com.felipedev.frauddetection.domain.port.out.FraudAnalysisRepositoryPort;
import com.felipedev.frauddetection.infrastructure.persistence.entity.FraudAnalysisEntity;
import com.felipedev.frauddetection.infrastructure.persistence.repository.FraudAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistência JPA para {@link FraudAnalysisRepositoryPort}.
 */
@Component
@RequiredArgsConstructor
public class JpaFraudAnalysisAdapter implements FraudAnalysisRepositoryPort {

    private final FraudAnalysisRepository repository;

    @Override
    public FraudAnalysisEntity save(FraudAnalysisEntity entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<FraudAnalysisEntity> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<FraudAnalysisEntity> findByTransactionId(String transactionId) {
        return repository.findByTransactionId(transactionId);
    }

    @Override
    public List<FraudAnalysisEntity> findByCustomerIdOrderByAnalyzedAtDesc(String customerId) {
        return repository.findByCustomerIdOrderByAnalyzedAtDesc(customerId);
    }

    @Override
    public long countRecentByCustomerId(String customerId, LocalDateTime since) {
        return repository.countByCustomerIdAndAnalyzedAtAfter(customerId, since);
    }

    @Override
    public Optional<FraudAnalysisEntity> findLatestByCustomerId(String customerId) {
        return repository.findFirstByCustomerIdOrderByAnalyzedAtDesc(customerId);
    }

    @Override
    public List<FraudAnalysisEntity> findRecentByCustomerId(String customerId, int limit) {
        return repository.findTop10ByCustomerIdOrderByAnalyzedAtDesc(customerId);
    }
}
