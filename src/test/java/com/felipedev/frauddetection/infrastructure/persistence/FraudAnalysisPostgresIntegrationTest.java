package com.felipedev.frauddetection.infrastructure.persistence;

import com.felipedev.frauddetection.domain.model.BlacklistType;
import com.felipedev.frauddetection.domain.model.Decision;
import com.felipedev.frauddetection.domain.model.PaymentMethod;
import com.felipedev.frauddetection.infrastructure.persistence.entity.BlacklistEntity;
import com.felipedev.frauddetection.infrastructure.persistence.entity.FraudAnalysisEntity;
import com.felipedev.frauddetection.infrastructure.persistence.repository.BlacklistRepository;
import com.felipedev.frauddetection.infrastructure.persistence.repository.FraudAnalysisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class FraudAnalysisPostgresIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("frauddetection_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        }
    }

    @Autowired
    private FraudAnalysisRepository analysisRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Test
    @DisplayName("Deve persistir e recuperar entidade de análise com Flyway schema e integridade relacional")
    void shouldPersistAndRetrieveFraudAnalysis() {
        FraudAnalysisEntity entity = FraudAnalysisEntity.builder()
                .transactionId("TX-CONTAINER-01")
                .customerId("CUST-TC-99")
                .customerCpf("12345678909")
                .amount(new BigDecimal("1500.00"))
                .paymentMethod(PaymentMethod.PIX)
                .riskScore(15)
                .decision(Decision.APPROVED)
                .decisionDescription("Aprovado")
                .latitude(-23.5505)
                .longitude(-46.6333)
                .analyzedAt(LocalDateTime.now())
                .build();

        FraudAnalysisEntity saved = analysisRepository.save(entity);

        assertNotNull(saved.getId());
        Optional<FraudAnalysisEntity> found = analysisRepository.findByTransactionId("TX-CONTAINER-01");
        assertTrue(found.isPresent());
        assertEquals("CUST-TC-99", found.get().getCustomerId());
        assertEquals(new BigDecimal("1500.00"), found.get().getAmount());
    }

    @Test
    @DisplayName("Deve gerenciar entidades da Blacklist com verificação ativa")
    void shouldManageBlacklistEntities() {
        BlacklistEntity blacklist = BlacklistEntity.builder()
                .type(BlacklistType.CPF)
                .value("99988877766")
                .reason("Fraude confirmada via contestação")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        blacklistRepository.save(blacklist);

        assertTrue(blacklistRepository.existsByTypeAndValueAndActiveTrue(BlacklistType.CPF, "99988877766"));
    }
}
