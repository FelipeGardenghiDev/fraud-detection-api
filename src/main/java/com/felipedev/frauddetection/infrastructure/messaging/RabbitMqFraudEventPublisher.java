package com.felipedev.frauddetection.infrastructure.messaging;

import com.felipedev.frauddetection.domain.model.Decision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de mensageria RabbitMQ para publicação de eventos de fraude.
 * Implementa a porta {@link FraudEventPublisher} segundo os princípios da Arquitetura Hexagonal.
 */
@Slf4j
@Component
public class RabbitMqFraudEventPublisher implements FraudEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fraud.messaging.enabled:true}")
    private boolean messagingEnabled;

    public RabbitMqFraudEventPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
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
            log.warn("Falha de conexão com RabbitMQ para o evento TxID={}: {}",
                    event.getTransactionId(), ex.getMessage());
            throw ex;
        }
    }
}
