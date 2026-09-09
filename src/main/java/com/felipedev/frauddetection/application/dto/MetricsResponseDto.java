package com.felipedev.frauddetection.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Métricas consolidadas do motor antifraude")
public class MetricsResponseDto {

    @Schema(description = "Total de transações avaliadas", example = "15420")
    private long totalAnalyzed;

    @Schema(description = "Total de transações aprovadas", example = "14200")
    private long totalApproved;

    @Schema(description = "Total de transações sob suspeita", example = "950")
    private long totalSuspicious;

    @Schema(description = "Total de transações bloqueadas", example = "270")
    private long totalBlocked;

    @Schema(description = "Taxa percentual de bloqueio de fraudes", example = "1.75")
    private double blockRatePercentage;

    @Schema(description = "Volume total financeiro protegido contra fraudes (BRL)", example = "3540000.00")
    private BigDecimal totalBlockedAmount;
}