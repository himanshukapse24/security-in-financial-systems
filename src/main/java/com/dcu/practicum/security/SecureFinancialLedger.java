package com.dcu.practicum.security;

import com.dcu.practicum.model.FinancialTransaction;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SecureFinancialLedger {
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final List<LedgerBlock> chain = new ArrayList<>();
    private final String secretKey;
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    public SecureFinancialLedger(String key) {
        this.secretKey = key;
    }

    // Add transaction to ledger
    public synchronized void commitTransaction(FinancialTransaction tx) throws Exception {
        int index = chain.size();
        String prevHash = chain.isEmpty() ? GENESIS_HASH : chain.get(chain.size() - 1).getCurrentHash();

        long timestamp = System.currentTimeMillis();
        String signature = signTx(tx, secretKey);
        String hash = hashTx(tx, prevHash, signature, index, timestamp);

        LedgerBlock block = new LedgerBlock(tx, prevHash, hash, index, timestamp);
        chain.add(block);
    }

    private String signTx(FinancialTransaction tx, String key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256", "BC");
        mac.init(keySpec);
        String data = tx.transactionId() + "::" + tx.originalRawPayload();
        return Hex.toHexString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String hashTx(FinancialTransaction tx, String prevHash, String sig, int index, long timestamp) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
        String payload = index + "|" + timestamp + "|" + prevHash + "|" + sig + "|" + tx.originalRawPayload();
        return Hex.toHexString(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
    }
}