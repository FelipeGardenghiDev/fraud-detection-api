package com.felipedev.frauddetection.infrastructure.messaging;

import com.felipedev.frauddetection.domain.port.out.FraudEventPublisherPort;

/**
 * Interface compatível com o Outbound Port para publicação de alertas antifraude.
 */
public interface FraudEventPublisher extends FraudEventPublisherPort {
}