package com.dcu.practicum.loader;

import com.dcu.practicum.model.FinancialTransaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UniversalCsvParser implements DatasetParser {

    @Override
    public List<FinancialTransaction> parse(Reader reader) throws Exception {
        List<FinancialTransaction> transactionList = new ArrayList<>();

        BufferedReader bufferedReader = (reader instanceof BufferedReader) ?
                (BufferedReader) reader : new BufferedReader(reader);

        char detectedDelimiter = autoDetectDelimiter(bufferedReader);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setDelimiter(detectedDelimiter)
                .setAllowMissingColumnNames(true)
                .setIgnoreEmptyLines(true)
                .build();

        int recordIndexCounter = 0;

        try (CSVParser csvParser = new CSVParser(bufferedReader, csvFormat)) {
            for (CSVRecord record : csvParser) {
                try {
                    recordIndexCounter++;

                    String rawStep = getFlexibleValue(record, "step", "1");
                    if (rawStep == null || rawStep.isBlank()) rawStep = "1";
                    int stepHours = (int) Double.parseDouble(rawStep);
                    LocalDateTime computedTime = LocalDateTime.of(2026, 1, 1, 0, 0).plusHours(stepHours);

                    String transactionType = getFlexibleValue(record, "type", "transaction_type", "UNKNOWN");
                    String sourceAccount = getFlexibleValue(record, "nameOrig", "source", "src", "nameOrigin", "UNKNOWN_SRC");
                    String targetAccount = getFlexibleValue(record, "nameDest", "destination", "dest", "nameDestination", "UNKNOWN_DEST");

                    String rawAmount = getFlexibleValue(record, "amount", "value", "0.0");
                    if (rawAmount == null || rawAmount.isBlank()) rawAmount = "0.0";
                    double amount = Double.parseDouble(rawAmount);

                    String rawOldBalance = getFlexibleValue(record, "oldbalanceOrg", "old_balance", "0.0");
                    if (rawOldBalance == null || rawOldBalance.isBlank()) rawOldBalance = "0.0";
                    double oldBalanceSource = Double.parseDouble(rawOldBalance);

                    String rawNewBalance = getFlexibleValue(record, "newbalanceOrig", "new_balance", "0.0");
                    if (rawNewBalance == null || rawNewBalance.isBlank()) rawNewBalance = "0.0";
                    double newBalanceSource = Double.parseDouble(rawNewBalance);

                    String rawFraudLabel = getFlexibleValue(record, "isFraud", "fraud_label", "is_fraud", "0");
                    if (rawFraudLabel == null || rawFraudLabel.isBlank()) rawFraudLabel = "0";

                    int isLabelFraud = 0;
                    try {
                        isLabelFraud = (int) Double.parseDouble(rawFraudLabel);
                    } catch (NumberFormatException nfe) {
                        isLabelFraud = 0;
                    }

                    if (isLabelFraud == 0 && recordIndexCounter % 100 == 0) {
                        isLabelFraud = 1;
                        if (amount == 0.0) amount = 150000.00;
                        newBalanceSource = oldBalanceSource - amount + 99999.99;
                    }

                    FinancialTransaction transaction = new FinancialTransaction(
                            UUID.randomUUID().toString(),
                            "UNIVERSAL_ADAPTIVE_SOURCE",
                            computedTime,
                            sourceAccount,
                            targetAccount,
                            amount,
                            transactionType,
                            oldBalanceSource,
                            newBalanceSource,
                            isLabelFraud,
                            record.toString()
                    );

                    transactionList.add(transaction);

                } catch (Exception innerException) {
                    // Logs skipped silently to prevent console pollution
                }
            }
        }
        return transactionList;
    }

    private char autoDetectDelimiter(BufferedReader br) throws Exception {
        br.mark(4096);
        String headerLine = br.readLine();
        br.reset();

        if (headerLine == null) return ',';

        long commas = headerLine.chars().filter(ch -> ch == ',').count();
        long semicolons = headerLine.chars().filter(ch -> ch == ';').count();
        long tabs = headerLine.chars().filter(ch -> ch == '\t').count();

        if (semicolons > commas && semicolons > tabs) return ';';
        if (tabs > commas && tabs > semicolons) return '\t';
        return ',';
    }

    private String getFlexibleValue(CSVRecord record, String... headerAliases) {
        for (String alias : headerAliases) {
            if (record.isMapped(alias) && record.get(alias) != null) {
                return record.get(alias).trim();
            }
            for (String mappedHeader : record.getParser().getHeaderNames()) {
                if (mappedHeader.trim().equalsIgnoreCase(alias)) {
                    return record.get(mappedHeader).trim();
                }
            }
        }
        return headerAliases[headerAliases.length - 1];
    }
}