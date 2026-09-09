package com.felipedev.frauddetection.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String FRAUD_EXCHANGE = "fraud.exchange";
    public static final String BLOCKED_QUEUE = "fraud.blocked.queue";
    public static final String SUSPICIOUS_QUEUE = "fraud.suspicious.queue";
    public static final String ROUTING_KEY_BLOCKED = "fraud.alert.blocked";
    public static final String ROUTING_KEY_SUSPICIOUS = "fraud.alert.suspicious";

    @Bean
    public DirectExchange fraudExchange() {
        return new DirectExchange(FRAUD_EXCHANGE);
    }

    @Bean
    public Queue blockedQueue() {
        return QueueBuilder.durable(BLOCKED_QUEUE).build();
    }

    @Bean
    public Queue suspiciousQueue() {
        return QueueBuilder.durable(SUSPICIOUS_QUEUE).build();
    }

    @Bean
    public Binding blockedBinding(Queue blockedQueue, DirectExchange fraudExchange) {
        return BindingBuilder.bind(blockedQueue).to(fraudExchange).with(ROUTING_KEY_BLOCKED);
    }

    @Bean
    public Binding suspiciousBinding(Queue suspiciousQueue, DirectExchange fraudExchange) {
        return BindingBuilder.bind(suspiciousQueue).to(fraudExchange).with(ROUTING_KEY_SUSPICIOUS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}