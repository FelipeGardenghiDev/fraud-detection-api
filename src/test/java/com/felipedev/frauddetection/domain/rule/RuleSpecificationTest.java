package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleSpecificationTest {

    @Test
    @DisplayName("Deve combinar especificações de regra com AND, OR e NOT")
    void shouldCombineSpecificationsWithAndOrNot() {
        RuleSpecification highAmountSpec = (req, ctx) ->
                req.getAmount() != null && req.getAmount().compareTo(new BigDecimal("1000.00")) >= 0;

        RuleSpecification blacklistSpec = (req, ctx) -> ctx.isCpfBlacklisted();

        RuleSpecification combinedAndSpec = highAmountSpec.and(blacklistSpec);
        RuleSpecification combinedOrSpec = highAmountSpec.or(blacklistSpec);
        RuleSpecification notHighAmountSpec = highAmountSpec.not();

        TransactionAnalysisRequestDto req = TransactionAnalysisRequestDto.builder()
                .amount(new BigDecimal("1500.00"))
                .build();

        AnalysisContext ctxBlacklisted = AnalysisContext.builder().cpfBlacklisted(true).build();
        AnalysisContext ctxClean = AnalysisContext.builder().cpfBlacklisted(false).build();

        // AND
        assertTrue(combinedAndSpec.isSatisfiedBy(req, ctxBlacklisted));
        assertFalse(combinedAndSpec.isSatisfiedBy(req, ctxClean));

        // OR
        assertTrue(combinedOrSpec.isSatisfiedBy(req, ctxClean));

        // NOT
        assertFalse(notHighAmountSpec.isSatisfiedBy(req, ctxClean));
    }
}
