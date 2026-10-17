package com.felipedev.frauddetection.domain.port.out;

import com.felipedev.frauddetection.domain.model.BlacklistType;

import java.util.Optional;

/**
 * Porta de saída (Outbound Port) para consulta e checagem de entidades na Blacklist.
 */
public interface BlacklistPort {

    boolean existsActive(BlacklistType type, String value);

    Optional<String> findActiveReason(BlacklistType type, String value);
}
