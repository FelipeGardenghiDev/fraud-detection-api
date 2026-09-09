package com.felipedev.frauddetection.domain.rule;

import com.felipedev.frauddetection.application.dto.TransactionAnalysisRequestDto;
import com.felipedev.frauddetection.domain.model.AnalysisContext;
import com.felipedev.frauddetection.domain.model.RuleExecutionResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class BlacklistRule implements FraudRule {

    public static final String RULE_NAME = "BLACKLIST_CHECK";

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public RuleExecutionResult evaluate(TransactionAnalysisRequestDto request, AnalysisContext context) {
        if (context.isCpfBlacklisted()) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    100,
                    "CPF do cliente cadastrado na lista restritiva (Blacklist): " + context.getBlacklistReason()
            );
        }

        if (context.isIpBlacklisted()) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    100,
                    "Endereço IP associado a atividades fraudulentas confirmadas na Blacklist."
            );
        }

        if (context.isDeviceBlacklisted()) {
            return RuleExecutionResult.triggered(
                    RULE_NAME,
                    100,
                    "Dispositivo (Device Fingerprint) listado como comprometido na Blacklist."
            );
        }

        return RuleExecutionResult.notTriggered(RULE_NAME);
    }
}