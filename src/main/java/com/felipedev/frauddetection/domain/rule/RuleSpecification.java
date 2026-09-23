package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;

/**
 * Padrão Specification (Domain-Driven Design).
 * Permite encapsular predicados de negócio e combiná-los de forma declarativa (AND, OR, NOT).
 */
@FunctionalInterface
public interface RuleSpecification {

    boolean isSatisfiedBy(TransactionAnalysisRequestDto request, AnalysisContext context);

    default RuleSpecification and(RuleSpecification other) {
        return (request, context) -> this.isSatisfiedBy(request, context) && other.isSatisfiedBy(request, context);
    }

    default RuleSpecification or(RuleSpecification other) {
        return (request, context) -> this.isSatisfiedBy(request, context) || other.isSatisfiedBy(request, context);
    }

    default RuleSpecification not() {
        return (request, context) -> !this.isSatisfiedBy(request, context);
    }
}
