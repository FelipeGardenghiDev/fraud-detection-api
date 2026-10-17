package com.felipedev.frauddetection.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Contexto enriquecido para avaliação das regras do motor antifraude.
 * Contém dados agregados da sessão, histórico do cliente e estado de rede.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisContext {

    private int recentTransactionCount;
    private boolean cpfBlacklisted;
    private boolean ipBlacklisted;
    private boolean deviceBlacklisted;
    private String blacklistReason;

    // Campos de Inteligência Geográfica (Impossible Travel)
    private Double lastTransactionLatitude;
    private Double lastTransactionLongitude;
    private LocalDateTime lastTransactionTime;

    // Campos de Análise Comportamental (Perfil do Cliente & Z-Score)
    private BigDecimal customerHistoricalAverage;
    private BigDecimal customerHistoricalStdDev;
    private int customerHistoricalCount;
}