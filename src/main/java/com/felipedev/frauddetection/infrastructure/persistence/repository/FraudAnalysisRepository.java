package com.felipedev.frauddetection.infrastructure.persistence.repository;

import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.infrastructure.persistence.entity.FraudAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysisEntity, UUID> {

    Optional<FraudAnalysisEntity> findByTransactionId(String transactionId);

    List<FraudAnalysisEntity> findByCustomerIdOrderByAnalyzedAtDesc(String customerId);

    long countByCustomerIdAndAnalyzedAtAfter(String customerId, LocalDateTime since);

    long countByDecision(Decision decision);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FraudAnalysisEntity f WHERE f.decision = :decision")
    BigDecimal sumAmountByDecision(@Param("decision") Decision decision);
}