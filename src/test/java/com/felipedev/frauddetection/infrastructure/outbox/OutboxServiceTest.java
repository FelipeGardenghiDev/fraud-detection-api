package com.felipedev.frauddetection.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private ObjectMapper objectMapper;
    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new OutboxService(outboxEventRepository, objectMapper);
    }

    @Test
    @DisplayName("Deve enfileirar evento de domínio na tabela de Outbox com status PENDING")
    void shouldEnqueueEventCorrectly() {
        UUID aggregateId = UUID.randomUUID();
        Map<String, String> payloadObj = Map.of("key", "value", "txId", "TX-123");

        OutboxEventEntity mockSaved = OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("FRAUD_ANALYSIS")
                .aggregateId(aggregateId)
                .eventType("FRAUD_ALERT")
                .payload("{\"key\":\"value\",\"txId\":\"TX-123\"}")
                .status(OutboxStatus.PENDING)
                .build();

        when(outboxEventRepository.save(any(OutboxEventEntity.class))).thenReturn(mockSaved);

        OutboxEventEntity result = outboxService.enqueue("FRAUD_ANALYSIS", aggregateId, "FRAUD_ALERT", payloadObj);

        assertNotNull(result);
        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEventEntity captured = captor.getValue();
        assertEquals("FRAUD_ANALYSIS", captured.getAggregateType());
        assertEquals(aggregateId, captured.getAggregateId());
        assertEquals("FRAUD_ALERT", captured.getEventType());
        assertEquals(OutboxStatus.PENDING, captured.getStatus());
        assertEquals(0, captured.getRetryCount());
    }
}
