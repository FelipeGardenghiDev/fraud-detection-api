package com.felipedev.frauddetection.domain.port.in;

import com.felipedev.frauddetection.application.dto.FraudAnalysisResponseDto;
import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;

import java.util.List;
import java.util.UUID;

/**
 * Caso de Uso Inbound (Primary Port) para análise e consulta de transações no motor antifraude.
 */
public interface AnalyzeTransactionUseCase {

    /**
     * Executa a análise antifraude completa para uma transação.
     *
     * @param request dados da transação a ser avaliada
     * @return resultado consolidado com score, veredito e detalhamento das regras
     */
    FraudAnalysisResponseDto analyzeTransaction(TransactionAnalysisRequestDto request);

    /**
     * Recupera o resultado de uma análise prévia pelo seu identificador UUID.
     *
     * @param id identificador único da análise
     * @return detalhes da análise realizada
     */
    FraudAnalysisResponseDto getById(UUID id);

    /**
     * Recupera o histórico de análises de um cliente.
     *
     * @param customerId identificador único do cliente
     * @return lista de análises ordenadas da mais recente para a mais antiga
     */
    List<FraudAnalysisResponseDto> getByCustomerId(String customerId);
}
