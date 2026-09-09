package com.felipedev.frauddetection.infrastructure.config;

import com.felipedev.frauddetection.domain.model.BlacklistType;
import com.felipedev.frauddetection.infrastructure.persistence.entity.BlacklistEntity;
import com.felipedev.frauddetection.infrastructure.persistence.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BlacklistRepository blacklistRepository;

    @Override
    public void run(String... args) {
        if (blacklistRepository.count() == 0) {
            log.info("Populando dados simulados na Blacklist para testes iniciais...");

            List<BlacklistEntity> seeds = List.of(
                    BlacklistEntity.builder()
                            .type(BlacklistType.CPF)
                            .value("12345678900")
                            .reason("Fraude PIX confirmada em boletim de ocorrência policial.")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
                    BlacklistEntity.builder()
                            .type(BlacklistType.CPF)
                            .value("98765432100")
                            .reason("Conta laranja identificada em lavagem de dinheiro.")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
                    BlacklistEntity.builder()
                            .type(BlacklistType.IP_ADDRESS)
                            .value("192.168.1.200")
                            .reason("IP identificado em múltiplos ataques de Credential Stuffing.")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
                    BlacklistEntity.builder()
                            .type(BlacklistType.DEVICE_FINGERPRINT)
                            .value("device-compromised-99")
                            .reason("Emulador com root utilizado em clonagem de cartões de crédito.")
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            blacklistRepository.saveAll(seeds);
            log.info("Carga inicial concluída com {} registros ativos na Blacklist!", seeds.size());
        }
    }
}