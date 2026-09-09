package com.felipedev.frauddetection.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "fraud.messaging.listener.enabled", havingValue = "true", matchIfMissing = false)
public class FraudAlertNotificationListener {

    @RabbitListener(queues = RabbitMqConfig.BLOCKED_QUEUE)
    public void handleBlockedAlert(FraudAlertEvent event) {
        log.warn("🚨 [CONSUMIDOR NOTIFICAÇÕES] Alerta de Fraude Crítica recebido via fila: Transação {} do cliente {} foi BLOQUEADA (Score: {}). Motivo: {}",
                event.getTransactionId(), event.getCustomerId(), event.getRiskScore(), event.getReason());
    }

    @RabbitListener(queues = RabbitMqConfig.SUSPICIOUS_QUEUE)
    public void handleSuspiciousAlert(FraudAlertEvent event) {
        log.info("⚠️ [CONSUMIDOR NOTIFICAÇÕES] Alerta de Transação Suspeita recebido: Transação {} do cliente {} encaminhada para análise manual.",
                event.getTransactionId(), event.getCustomerId());
    }
}