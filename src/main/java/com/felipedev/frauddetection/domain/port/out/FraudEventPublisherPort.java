package com.felipedev.frauddetection.domain.port.out;

import com.felipedev.frauddetection.infrastructure.messaging.FraudAlertEvent;

/**
 * Porta de saída (Outbound Port) para publicação de alertas antifraude em brokers assíncronos.
 */
public interface FraudEventPublisherPort {

    void publishAlert(FraudAlertEvent event);
}
