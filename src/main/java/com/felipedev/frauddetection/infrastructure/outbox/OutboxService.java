package com.felipedev.frauddetection.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Enfileira um evento na tabela de Outbox para garantir consistência transacional atômica (ACID)
     * com a persistência da entidade de análise no banco de dados.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEventEntity enqueue(String aggregateType, UUID aggregateId, String eventType, Object eventPayload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventPayload);

            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            OutboxEventEntity saved = outboxEventRepository.save(outboxEvent);
            log.debug("Evento gravado no Outbox com sucesso: ID={}, Type={}, AggregateId={}",
                    saved.getId(), eventType, aggregateId);
            return saved;
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar payload para o Outbox: aggregateId={}, type={}", aggregateId, eventType, e);
            throw new IllegalArgumentException("Falha ao serializar payload do evento para o Outbox", e);
        }
    }
}
