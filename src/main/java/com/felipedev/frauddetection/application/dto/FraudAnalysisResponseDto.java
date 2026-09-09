package com.felipedev.frauddetection.application.dto;

import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado consolidado da avaliação antifraude")
public class FraudAnalysisResponseDto {

    @Schema(description = "ID único da análise", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID analysisId;

    @Schema(description = "ID da transação analisada", example = "TX-98471203")
    private String transactionId;

    @Schema(description = "ID do cliente", example = "CUST-4029")
    private String customerId;

    @Schema(description = "Pontuação de risco de 0 a 100", example = "85")
    private int riskScore;

    @Schema(description = "Decisão final do motor", example = "BLOCKED")
    private Decision decision;

    @Schema(description = "Motivo ou recomendação da decisão", example = "Transação bloqueada devido a alto risco de fraude.")
    private String decisionDescription;

    @Schema(description = "Detalhamento de todas as regras avaliadas")
    private List<RuleExecutionResult> ruleBreakdown;

    @Schema(description = "Indica se o resultado foi recuperado de uma requisição anterior idêntica (Idempotência)", example = "false")
    private boolean idempotencyHit;

    @Schema(description = "Data e hora do processamento", example = "2026-09-09T15:20:00")
    private LocalDateTime analyzedAt;
}