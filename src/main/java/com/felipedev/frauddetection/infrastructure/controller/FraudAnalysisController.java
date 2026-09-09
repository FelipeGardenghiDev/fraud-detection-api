package com.felipedev.frauddetection.infrastructure.controller;

import com.felipedev.frauddetection.application.dto.FraudAnalysisResponseDto;
import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.application.service.FraudAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud-analyses")
@RequiredArgsConstructor
@Tag(name = "Análises Antifraude", description = "Endpoints para submissão e consulta de análises de risco de transações")
public class FraudAnalysisController {

    private final FraudAnalysisService service;

    @PostMapping
    @Operation(summary = "Submeter transação para avaliação antifraude em tempo real",
            description = "Executa a esteira de regras de negócio (Rule Engine) e retorna o Risk Score com a decisão de aprovação ou bloqueio.")
    @ApiResponse(responseCode = "200", description = "Avaliação executada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos ou ausentes na requisição")
    public ResponseEntity<FraudAnalysisResponseDto> analyzeTransaction(
            @Valid @RequestBody TransactionAnalysisRequestDto request) {
        FraudAnalysisResponseDto response = service.analyzeTransaction(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar detalhes de uma análise específica por ID")
    @ApiResponse(responseCode = "200", description = "Análise localizada")
    @ApiResponse(responseCode = "404", description = "Análise não encontrada")
    public ResponseEntity<FraudAnalysisResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Listar histórico de avaliações antifraude de um cliente específico")
    public ResponseEntity<List<FraudAnalysisResponseDto>> getByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(service.getByCustomerId(customerId));
    }
}