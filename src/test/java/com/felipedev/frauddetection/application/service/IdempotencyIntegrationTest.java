package com.felipedev.frauddetection.application.service;

import com.felipedev.frauddetection.application.dto.FraudAnalysisResponseDto;
import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class IdempotencyIntegrationTest {

    @Autowired
    private FraudAnalysisService fraudAnalysisService;

    @Test
    @DisplayName("Deve reprocessar na primeira chamada e retornar resultado em cache (Idempotency Hit) na segunda chamada com mesmo transactionId")
    void shouldReturnCachedAnalysisOnDuplicateTransaction() {
        String txId = "TX-IDEM-TEST-001";

        TransactionAnalysisRequestDto request = TransactionAnalysisRequestDto.builder()
                .transactionId(txId)
                .customerId("CUST-IDEM-10")
                .customerCpf("11122233344")
                .amount(new BigDecimal("250.00"))
                .paymentMethod(PaymentMethod.PIX)
                .build();

        // 1ª Chamada: Análise normal processada
        FraudAnalysisResponseDto firstResponse = fraudAnalysisService.analyzeTransaction(request);

        assertNotNull(firstResponse);
        assertNotNull(firstResponse.getAnalysisId());
        assertFalse(firstResponse.isIdempotencyHit(), "Primeira requisição não deve ser Idempotency Hit");

        // 2ª Chamada: Idempotência ativada
        FraudAnalysisResponseDto secondResponse = fraudAnalysisService.analyzeTransaction(request);

        assertNotNull(secondResponse);
        assertTrue(secondResponse.isIdempotencyHit(), "Segunda requisição com mesmo TX_ID deve ser Idempotency Hit");
        assertEquals(firstResponse.getAnalysisId(), secondResponse.getAnalysisId(), "IDs da análise devem ser idênticos");
        assertEquals(firstResponse.getRiskScore(), secondResponse.getRiskScore(), "Score de risco deve ser idêntico");
        assertEquals(firstResponse.getDecision(), secondResponse.getDecision(), "Decisão de risco deve ser idêntica");
    }
}