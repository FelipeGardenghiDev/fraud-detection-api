package com.felipedev.frauddetection.application.dto;

import com.felipedev.frauddetection.domain.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados da transação a ser analisada pelo motor antifraude")
public class TransactionAnalysisRequestDto {

    @NotBlank(message = "transactionId é obrigatório")
    @Schema(description = "Identificador único da transação", example = "TX-98471203")
    private String transactionId;

    @NotBlank(message = "customerId é obrigatório")
    @Schema(description = "Identificador do cliente", example = "CUST-4029")
    private String customerId;

    @NotBlank(message = "customerCpf é obrigatório")
    @Schema(description = "CPF do pagador/cliente", example = "12345678900")
    private String customerCpf;

    @NotNull(message = "amount é obrigatório")
    @Positive(message = "amount deve ser um valor positivo maior que zero")
    @Schema(description = "Valor da transação em Reais (BRL)", example = "12500.00")
    private BigDecimal amount;

    @NotNull(message = "paymentMethod é obrigatório (PIX, CREDIT_CARD, DEBIT_CARD, BOLETO)")
    @Schema(description = "Método de pagamento", example = "PIX")
    private PaymentMethod paymentMethod;

    @Schema(description = "Endereço IP de origem da transação", example = "187.55.120.4")
    private String ipAddress;

    @Schema(description = "Fingerprint único do dispositivo do usuário", example = "dev-fp-88a7c29")
    private String deviceFingerprint;

    @Schema(description = "Localização geográfica aproximada", example = "Ribeirão Preto, SP")
    private String location;

    @Schema(description = "Horário de ocorrência da transação (ISO-8601). Se nulo, utiliza o horário atual.", example = "2026-09-09T23:45:00")
    private LocalDateTime occurredAt;
}