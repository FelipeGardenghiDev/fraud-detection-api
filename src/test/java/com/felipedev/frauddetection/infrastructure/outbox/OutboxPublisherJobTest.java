package com.felipedev.frauddetection.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.domain.model.PaymentMethod;
import com.felipedev.frauddetection.infrastructure.messaging.FraudAlertEvent;
import com.felipedev.frauddetection.infrastructure.messaging.FraudEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherJobTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private FraudEventPublisher fraudEventPublisher;

    private ObjectMapper objectMapper;
    private OutboxPublisherJob publisherJob;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        publisherJob = new OutboxPublisherJob(outboxEventRepository, fraudEventPublisher, objectMapper);
        ReflectionTestUtils.setField(publisherJob, "maxRetries", 3);
    }

    @Test
    @DisplayName("Deve processar evento com sucesso e atualizar status para PROCESSED")
    void shouldProcessEventSuccessfully() throws Exception {
        UUID eventId = UUID.randomUUID();
        FraudAlertEvent alertEvent = FraudAlertEvent.builder()
                .analysisId(UUID.randomUUID())
                .transactionId("TX-100")
                .customerId("CUST-1")
                .amount(BigDecimal.valueOf(500))
                .paymentMethod(PaymentMethod.PIX)
                .riskScore(95)
                .decision(Decision.BLOCKED)
                .reason("Bloqueado")
                .timestamp(LocalDateTime.now())
                .build();

        String payload = objectMapper.writeValueAsString(alertEvent);

        OutboxEventEntity entity = OutboxEventEntity.builder()
                .id(eventId)
                .aggregateType("FRAUD_ANALYSIS")
                .aggregateId(alertEvent.getAnalysisId())
                .eventType("FRAUD_ALERT")
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));

        publisherJob.processPendingEvents();

        verify(fraudEventPublisher, times(1)).publishAlert(any(FraudAlertEvent.class));

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEventEntity saved = captor.getValue();
        assertEquals(OutboxStatus.PROCESSED, saved.getStatus());
        assertNotNull(saved.getProcessedAt());
    }

    @Test
    @DisplayName("Deve incrementar retry_count em caso de falha temporária no RabbitMQ")
    void shouldIncrementRetryCountOnFailure() {
        OutboxEventEntity entity = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("FRAUD_ANALYSIS")
                .aggregateId(UUID.randomUUID())
                .eventType("FRAUD_ALERT")
                .payload("invalid-json")
                .status(OutboxStatus.PENDING)
                .retryCount(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));

        publisherJob.processPendingEvents();

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEventEntity saved = captor.getValue();
        assertEquals(2, saved.getRetryCount());
        assertEquals(OutboxStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getErrorMessage());
    }

    @Test
    @DisplayName("Deve marcar evento como FAILED quando atingir o limite maxRetries (Dead Letter)")
    void shouldMarkAsFailedWhenMaxRetriesExceeded() {
        OutboxEventEntity entity = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("FRAUD_ANALYSIS")
                .aggregateId(UUID.randomUUID())
                .eventType("FRAUD_ALERT")
                .payload("invalid-json")
                .status(OutboxStatus.PENDING)
                .retryCount(2) // maxRetries é 3, portanto a próxima tentativa (3) deve marcar como FAILED
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));

        publisherJob.processPendingEvents();

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEventEntity saved = captor.getValue();
        assertEquals(3, saved.getRetryCount());
        assertEquals(OutboxStatus.FAILED, saved.getStatus());
    }
}
