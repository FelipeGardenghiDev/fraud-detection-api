package com.felipedev.frauddetection.application.dto;

import com.felipedev.frauddetection.domain.model.BlacklistType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para inclusão de item na Blacklist")
public class BlacklistRequestDto {

    @NotNull(message = "type é obrigatório (CPF, IP_ADDRESS, DEVICE_FINGERPRINT)")
    @Schema(description = "Tipo de identificador na blacklist", example = "CPF")
    private BlacklistType type;

    @NotBlank(message = "value é obrigatório")
    @Schema(description = "Valor a ser bloqueado (CPF, IP ou Device Fingerprint)", example = "12345678900")
    private String value;

    @NotBlank(message = "reason é obrigatório")
    @Schema(description = "Motivo do bloqueio", example = "Histórico recorrente de chargeback e estelionato.")
    private String reason;
}