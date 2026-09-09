package com.felipedev.frauddetection.infrastructure.messaging;

import com.felipedev.frauddetection.domain.model.Decision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FraudEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fraud.messaging.enabled:true}")
    private boolean messagingEnabled;

    public FraudEventPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAlert(FraudAlertEvent event) {
        if (!messagingEnabled || rabbitTemplate == null) {
            log.debug("Mensageria RabbitMQ inativa ou não configurada. Evento ignorado.");
            return;
        }

        String routingKey = event.getDecision() == Decision.BLOCKED
                ? RabbitMqConfig.ROUTING_KEY_BLOCKED
                : RabbitMqConfig.ROUTING_KEY_SUSPICIOUS;

        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.FRAUD_EXCHANGE, routingKey, event);
            log.info("Evento de fraude publicado no RabbitMQ: Exchange={}, RoutingKey={}, TxID={}",
                    RabbitMqConfig.FRAUD_EXCHANGE, routingKey, event.getTransactionId());
        } catch (Exception ex) {
            log.warn("Falha de conexão com RabbitMQ para o evento TxID={}. O microsserviço continuará operando. Causa: {}",
                    event.getTransactionId(), ex.getMessage());
        }
    }
}