package com.felipedev.frauddetection.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipedev.frauddetection.infrastructure.messaging.FraudAlertEvent;
import com.felipedev.frauddetection.infrastructure.messaging.FraudEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fraud.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final FraudEventPublisher fraudEventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${fraud.outbox.max-retries:5}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${fraud.outbox.poll-interval-ms:2000}")
    @Transactional
    public void processPendingEvents() {
        List<OutboxEventEntity> pendingEvents =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processando {} eventos pendentes no Outbox...", pendingEvents.size());

        for (OutboxEventEntity eventEntity : pendingEvents) {
            processSingleEvent(eventEntity);
        }
    }

    public void processSingleEvent(OutboxEventEntity eventEntity) {
        try {
            if ("FRAUD_ALERT".equalsIgnoreCase(eventEntity.getEventType())) {
                FraudAlertEvent alertEvent = objectMapper.readValue(eventEntity.getPayload(), FraudAlertEvent.class);
                fraudEventPublisher.publishAlert(alertEvent);
            }

            eventEntity.setStatus(OutboxStatus.PROCESSED);
            eventEntity.setProcessedAt(LocalDateTime.now());
            eventEntity.setErrorMessage(null);
            outboxEventRepository.save(eventEntity);

            log.info("Evento do Outbox processado com sucesso: ID={}, Type={}",
                    eventEntity.getId(), eventEntity.getEventType());
        } catch (Exception e) {
            int currentRetries = eventEntity.getRetryCount() + 1;
            eventEntity.setRetryCount(currentRetries);
            eventEntity.setErrorMessage(e.getMessage());

            if (currentRetries >= maxRetries) {
                eventEntity.setStatus(OutboxStatus.FAILED);
                log.error("Evento do Outbox ID={} falhou e atingiu o limite de {} tentativas. Marcado como FAILED.",
                        eventEntity.getId(), maxRetries, e);
            } else {
                log.warn("Falha temporária ao publicar evento ID={} (tentativa {}/{}). Será reprocessado no próximo ciclo. Erro: {}",
                        eventEntity.getId(), currentRetries, maxRetries, e.getMessage());
            }

            outboxEventRepository.save(eventEntity);
        }
    }
}
