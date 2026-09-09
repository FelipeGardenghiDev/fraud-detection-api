package com.felipedev.frauddetection.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalysisContext {
    private int recentTransactionCount;
    private boolean cpfBlacklisted;
    private boolean ipBlacklisted;
    private boolean deviceBlacklisted;
    private String blacklistReason;
}