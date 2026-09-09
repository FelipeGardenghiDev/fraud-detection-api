package com.felipedev.frauddetection.infrastructure.messaging;

import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.domain.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlertEvent implements Serializable {

    private UUID analysisId;
    private String transactionId;
    private String customerId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private int riskScore;
    private Decision decision;
    private String reason;
    private LocalDateTime timestamp;
}