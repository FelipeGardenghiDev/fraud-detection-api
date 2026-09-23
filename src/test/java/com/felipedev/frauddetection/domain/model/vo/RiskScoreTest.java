package com.felipedev.frauddetection.domain.model.vo;

import com.felipedev.frauddetection.domain.model.Decision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoreTest {

    @Test
    @DisplayName("Deve limitar o score entre 0 e 100")
    void shouldClampScoreBoundaries() {
        assertEquals(0, RiskScore.of(-20).getValue());
        assertEquals(100, RiskScore.of(150).getValue());
        assertEquals(55, RiskScore.of(55).getValue());
    }

    @Test
    @DisplayName("Deve determinar corretamente decisões conforme faixas de risco")
    void shouldMapDecisionCorrectly() {
        RiskScore low = RiskScore.of(25);
        assertTrue(low.isApproved());
        assertFalse(low.isSuspicious());
        assertFalse(low.isBlocked());
        assertEquals(Decision.APPROVED, low.toDecision());

        RiskScore medium = RiskScore.of(65);
        assertFalse(medium.isApproved());
        assertTrue(medium.isSuspicious());
        assertFalse(medium.isBlocked());
        assertEquals(Decision.SUSPICIOUS, medium.toDecision());

        RiskScore high = RiskScore.of(90);
        assertFalse(high.isApproved());
        assertFalse(high.isSuspicious());
        assertTrue(high.isBlocked());
        assertEquals(Decision.BLOCKED, high.toDecision());
    }
}
