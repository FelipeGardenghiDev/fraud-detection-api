package com.felipedev.frauddetection.application.dto;

import com.felipedev.frauddetection.domain.model.BlacklistType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item registrado na Blacklist")
public class BlacklistResponseDto {

    private UUID id;
    private BlacklistType type;
    private String value;
    private String reason;
    private boolean active;
    private LocalDateTime createdAt;
}