package com.felipedev.frauddetection.infrastructure.persistence.entity;

import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.domain.model.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fraud_analyses", indexes = {
        @Index(name = "idx_tx_id", columnList = "transaction_id"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_analyzed_at", columnList = "analyzed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_cpf", nullable = false)
    private String customerCpf;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    private String location;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    @Column(name = "decision_description", length = 500)
    private String decisionDescription;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "analysis_id")
    private List<ExecutedRuleEntity> executedRules = new ArrayList<>();

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
}