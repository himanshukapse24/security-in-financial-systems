package com.dcu.practicum.security;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AdversarialSimulationBot implements Runnable {
    private final String dataFile;
    private final String backupFile;
    private final Random rand = new Random();
    private boolean running = true;

    public static final Object FILE_LOCK = new Object();
    private static final List<String> ATTACK_COLS = Arrays.asList(
            "trans_date_trans_time", "nameOrig", "type", "amount"
    );

    public AdversarialSimulationBot(String dataFile, String backupFile) {
        this.dataFile = dataFile;
        this.backupFile = backupFile;
    }

    @Override
    public void run() {

        try {
            File original = new File(dataFile);
            File backup = new File(backupFile);

            synchronized (FILE_LOCK) {
                Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            int attackCount = 0;
            CSVFormat fmt = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setAllowMissingColumnNames(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();

            while (running) {
                Thread.sleep(7000);
                if (!running) break;
                attackCount++;

                List<List<String>> rows = new ArrayList<>();
                List<String> headers = new ArrayList<>();

                synchronized (FILE_LOCK) {
                    try (CSVParser parser = fmt.parse(new FileReader(dataFile))) {
                        headers.addAll(parser.getHeaderNames());
                        for (CSVRecord record : parser) {
                            List<String> rowData = new ArrayList<>();
                            for (String h : headers) {
                                if (h != null && record.isMapped(h)) {
                                    rowData.add(record.get(h));
                                } else {
                                    rowData.add("");
                                }
                            }
                            rows.add(rowData);
                        }
                    }
                }

                if (rows.isEmpty()) continue;
                int rowIdx = rand.nextInt(rows.size());
                String colName = ATTACK_COLS.get(rand.nextInt(ATTACK_COLS.size()));
                int colIdx = -1;
                for (int i = 0; i < headers.size(); i++) {
                    if (headers.get(i) != null && headers.get(i).trim().equalsIgnoreCase(colName)) {
                        colIdx = i;
                        break;
                    }
                }

                if (colIdx == -1) {
                    colIdx = rand.nextInt(headers.size());
                    colName = headers.get(colIdx);
                }

                List<String> row = rows.get(rowIdx);
                if (colIdx >= row.size()) continue;

                String oldValue = row.get(colIdx);
                String newValue;
                if (colName.equalsIgnoreCase("amount")) {
                    newValue = String.format("%.2f", rand.nextDouble() * 500000.0);
                } else if (colName.equalsIgnoreCase("type")) {
                    newValue = oldValue.equalsIgnoreCase("TRANSFER") ? "CASH_OUT" : "TRANSFER";
                } else if (colName.equalsIgnoreCase("trans_date_trans_time")) {
                    newValue = "2026-01-01 00:00:00";
                } else {
                    newValue = "UNKNOWN_USER_" + rand.nextInt(999);
                }

                File tempFile = new File("dataset_temp.csv");
                CSVFormat outFmt = CSVFormat.DEFAULT.builder()
                        .setHeader(headers.toArray(new String[0]))
                        .setAllowMissingColumnNames(true)
                        .build();

                row.set(colIdx, newValue);
                try (CSVPrinter printer = new CSVPrinter(new FileWriter(tempFile), outFmt)) {
                    for (List<String> r : rows) {
                        printer.printRecord(r);
                    }
                    printer.flush();
                }

                synchronized (FILE_LOCK) {
                    String msg = String.format("Bot changed Row #%d | Column: [%s] | From '%s' to '%s'",
                            (rowIdx + 1), colName, oldValue, newValue);

                    PlatformUiBridge.pushInfo(msg, "Waiting for check...");
                    SharedAttackState.setAttack((rowIdx + 1), colName, oldValue, newValue);

                    Files.move(tempFile.toPath(), original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.printf("[BOT] Attack #%d - Row %d, Column: %s%n", attackCount, (rowIdx + 1), colName);
                }
            }
        } catch (Exception e) {
            System.err.println("[BOT ERROR] " + e.getMessage());
        }
    }
}