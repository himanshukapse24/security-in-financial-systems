package com.dcu.practicum.model;

import java.time.LocalDateTime;

public record FinancialTransaction(
        String transactionId,
        String datasetSource,
        LocalDateTime timestamp,
        String sourceAccount,
        String targetAccount,
        double amount,
        String transactionType,
        double oldBalanceSource,
        double newBalanceSource,
        int isLabelFraud,
        String originalRawPayload
) {}