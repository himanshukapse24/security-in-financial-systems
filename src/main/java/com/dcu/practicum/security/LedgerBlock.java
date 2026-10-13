package com.dcu.practicum.security;

import com.dcu.practicum.model.FinancialTransaction;

public class LedgerBlock {
    private final FinancialTransaction tx;
    private final String prevHash;
    private final String currentHash;
    private final int index;
    private final long timestamp;

    public LedgerBlock(FinancialTransaction tx, String prevHash, String currentHash, int index, long timestamp) {
        this.tx = tx;
        this.prevHash = prevHash;
        this.currentHash = currentHash;
        this.index = index;
        this.timestamp = timestamp;
    }

    public FinancialTransaction getTransaction() {
        return tx;
    }

    public String getPreviousHash() {
        return prevHash;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public int getBlockIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }
}