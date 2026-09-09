package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;

public interface FraudRule {

    String getName();

    int getOrder();

    RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context);
}