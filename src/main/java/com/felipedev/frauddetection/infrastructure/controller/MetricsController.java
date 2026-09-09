package com.felipedev.frauddetection.infrastructure.controller;

import com.felipedev.frauddetection.application.dto.MetricsResponseDto;
import com.felipedev.frauddetection.application.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Métricas e Telemetria", description = "Estatísticas operacionais e taxa de bloqueio do motor de fraude")
public class MetricsController {

    private final MetricsService service;

    @GetMapping("/overview")
    @Operation(summary = "Obter visão consolidada de métricas antifraude",
            description = "Retorna totais de transações processadas, aprovadas, suspeitas, taxa percentual de bloqueio e volume financeiro protegido.")
    public ResponseEntity<MetricsResponseDto> getOverview() {
        return ResponseEntity.ok(service.getOverview());
    }
}