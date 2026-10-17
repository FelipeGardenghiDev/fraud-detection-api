package com.felipedev.frauddetection.domain.port.in;

import com.felipedev.frauddetection.application.dto.BlacklistRequestDto;
import com.felipedev.frauddetection.application.dto.BlacklistResponseDto;
import com.felipedev.frauddetection.domain.model.BlacklistType;

import java.util.List;
import java.util.UUID;

/**
 * Caso de Uso Inbound (Primary Port) para gestão da lista de bloqueio (Blacklist).
 */
public interface ManageBlacklistUseCase {

    BlacklistResponseDto addToBlacklist(BlacklistRequestDto request);

    void removeFromBlacklist(UUID id);

    default void deactivate(UUID id) {
        removeFromBlacklist(id);
    }

    List<BlacklistResponseDto> getAllActive();

    default List<BlacklistResponseDto> listActive() {
        return getAllActive();
    }

    boolean isBlacklisted(BlacklistType type, String value);
}
