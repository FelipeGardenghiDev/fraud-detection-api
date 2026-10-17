package com.felipedev.frauddetection.domain.port.in;

import com.felipedev.frauddetection.application.dto.MetricsResponseDto;

/**
 * Caso de Uso Inbound (Primary Port) para consulta analítica de métricas operacionais e financeiras.
 */
public interface ConsultMetricsUseCase {

    MetricsResponseDto getOverview();
}
