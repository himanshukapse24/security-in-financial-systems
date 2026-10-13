package com.dcu.practicum;

import com.dcu.practicum.loader.UniversalCsvParser;
import com.dcu.practicum.model.FinancialTransaction;
import com.dcu.practicum.security.SecureFinancialLedger;
import com.dcu.practicum.security.AdversarialSimulationBot;
import com.dcu.practicum.security.SharedAttackState;
import com.dcu.practicum.analytics.AnomalyDetection;
import com.dcu.practicum.analytics.ModelEvaluationMetrics;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class MainApplication extends JFrame {
    private static final String MASTER_KEY = "CRYPTO_SECRET_KEY";
    private static final String DATA_FILE = "dataset3.csv";
    private static final String BACKUP_FILE = "dataset_backup_master.csv";

    private final JPanel statusPanel;
    private final JPanel cryptoPanel;
    private final JLabel statusValue;
    private final JLabel passCounter;
    private final JLabel cryptoStatus;

    private final JTextArea consoleOutput;
    private final JTextArea attackerInfo;
    private final JTextArea attackList;
    private final JButton maintenanceBtn;

    public static volatile boolean maintenanceMode = false;
    public static volatile boolean isPaused = false;
    private final UniversalCsvParser parser = new UniversalCsvParser();
    private List<FinancialTransaction> transactions;
    private int passCount = 0;
    private Timer updateTimer = null;

    public MainApplication() {
        super("Security Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.decode("#1E293B"));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("FINANCIAL SECURITY DASHBOARD");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        maintenanceBtn = new JButton("MAINTENANCE: OFF");
        maintenanceBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintenanceBtn.setBackground(Color.decode("#334155"));
        maintenanceBtn.setForeground(Color.WHITE);
        maintenanceBtn.setFocusPainted(false);
        maintenanceBtn.setContentAreaFilled(true);
        maintenanceBtn.setOpaque(true);
        maintenanceBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2, true),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        maintenanceBtn.addActionListener(e -> toggleMaintenance());
        headerPanel.add(maintenanceBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainContent = new JPanel(new BorderLayout(25, 25));
        mainContent.setBorder(new EmptyBorder(20, 25, 25, 25));
        mainContent.setBackground(Color.decode("#F1F5F9"));

        // Top cards row
        JPanel cardRow = new JPanel(new GridLayout(1, 3, 20, 0));
        cardRow.setOpaque(false);

        statusPanel = makeStatusCard("SECURITY STATUS", "#0284C7");
        statusValue = new JLabel("INITIALIZING...");
        statusValue.setForeground(Color.WHITE);
        statusValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statusPanel.add(statusValue);

        JPanel passPanel = makeStatusCard("PASS COUNT", "#475569");
        passCounter = new JLabel("Pass: 0");
        passCounter.setForeground(Color.WHITE);
        passCounter.setFont(new Font("Segoe UI", Font.BOLD, 36));
        passPanel.add(passCounter);

        cryptoPanel = makeStatusCard("CRYPTO LEDGER STATUS", "#059669");
        cryptoStatus = new JLabel("CHECKING");
        cryptoStatus.setForeground(Color.WHITE);
        cryptoStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cryptoPanel.add(cryptoStatus);

        cardRow.add(statusPanel);
        cardRow.add(passPanel);
        cardRow.add(cryptoPanel);
        mainContent.add(cardRow, BorderLayout.NORTH);

        // Bottom section
        JPanel bottomSection = new JPanel(new GridLayout(1, 2, 25, 0));
        bottomSection.setOpaque(false);

        // Left: Console
        JPanel consolePanel = new JPanel(new BorderLayout(0, 10));
        consolePanel.setOpaque(false);
        JLabel consoleLabel = new JLabel("System Log:");
        consoleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        consoleLabel.setForeground(Color.decode("#1E293B"));

        consoleOutput = new JTextArea();
        consoleOutput.setBackground(Color.decode("#0F172A"));
        consoleOutput.setForeground(Color.decode("#94A3B8"));
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 15));
        consoleOutput.setEditable(false);
        consoleOutput.setMargin(new Insets(12, 12, 12, 12));
        JScrollPane scrollConsole = new JScrollPane(consoleOutput);
        scrollConsole.setBorder(null);
        consolePanel.add(consoleLabel, BorderLayout.NORTH);
        consolePanel.add(scrollConsole, BorderLayout.CENTER);

        // Right: Info panels
        JPanel infoStack = new JPanel();
        infoStack.setOpaque(false);
        infoStack.setLayout(new BoxLayout(infoStack, BoxLayout.Y_AXIS));

        JPanel attackerPanel = makeInfoCard("Attacker Logs:");
        attackerInfo = makeTextArea("No attacks yet...");
        attackerPanel.add(new JScrollPane(attackerInfo), BorderLayout.CENTER);
        attackerPanel.setPreferredSize(new Dimension(500, 180));
        attackerPanel.setMaximumSize(new Dimension(2000, 180));

        JPanel listPanel = makeInfoCard("Attack Queue:");
        attackList = makeTextArea("Queue waiting...");
        listPanel.add(new JScrollPane(attackList), BorderLayout.CENTER);
        listPanel.setPreferredSize(new Dimension(500, 450));
        listPanel.setMaximumSize(new Dimension(2000, 2000));

        infoStack.add(attackerPanel);
        infoStack.add(Box.createVerticalStrut(20));
        infoStack.add(listPanel);

        bottomSection.add(consolePanel);
        bottomSection.add(infoStack);

        mainContent.add(bottomSection, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // Space bar to pause
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_SPACE) {
                isPaused = !isPaused;
                if (isPaused) {
                    printLog("[SYSTEM] Paused");
                } else {
                    printLog("[SYSTEM] Resumed");
                }
                return true;
            }
            return false;
        });

        // Timer for checks
        updateTimer = new Timer(3000, event -> {
            if (isPaused) return;
            if (passCount < 12) {
                runCheck();
            } else {
                updateTimer.stop();
                printLog("[SYSTEM] Checks done");
                printLog("[ML] Running analysis...");

                AnomalyDetection ml = new AnomalyDetection();
                ModelEvaluationMetrics poisoned = ml.evaluateModelPerformance(transactions, true);
                ModelEvaluationMetrics clean = ml.evaluateModelPerformance(transactions, false);

                printLog("--------------------------------------------");
                printLog("DATASET WITHOUT PROTECTION:");
                printLog("--------------------------------------------");
                printLog(String.format("True Positives: %d", poisoned.truePositives()));
                printLog(String.format("False Negatives: %d", poisoned.falseNegatives()));
                printLog(String.format("Precision: %.2f%%", poisoned.precision() * 100));
                printLog(String.format("Recall: %.2f%%", poisoned.recall() * 100));
                printLog(String.format("F1-Score: %.2f%%", poisoned.f1Score() * 100));

                printLog("--------------------------------------------");
                printLog("DATASET WITH PROTECTION:");
                printLog("--------------------------------------------");
                printLog(String.format("True Positives: %d", clean.truePositives()));
                printLog(String.format("False Negatives: %d", clean.falseNegatives()));
                printLog(String.format("Precision: %.2f%%", clean.precision() * 100));
                printLog(String.format("Recall: %.2f%%", clean.recall() * 100));
                printLog(String.format("F1-Score: %.2f%%", clean.f1Score() * 100));
                printLog("============================================");
            }
        });

        SwingUtilities.invokeLater(this::startSystem);
    }

    private void toggleMaintenance() {
        maintenanceMode = !maintenanceMode;
        if (maintenanceMode) {
            maintenanceBtn.setBackground(Color.decode("#EA580C"));
            maintenanceBtn.setText("MAINTENANCE: ON");
            printLog("[SYSTEM] Maintenance mode enabled");
        } else {
            maintenanceBtn.setBackground(Color.decode("#334155"));
            maintenanceBtn.setText("MAINTENANCE: OFF");
            printLog("[SYSTEM] Maintenance mode disabled");
        }
    }

    private JPanel makeStatusCard(String title, String color) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setBackground(Color.decode(color));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        JLabel label = new JLabel(title);
        label.setForeground(Color.decode("#CBD5E1"));
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label);
        return panel;
    }

    private JPanel makeInfoCard(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.decode("#F8FAFC"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#E2E8F0"), 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.decode("#334155"));
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JTextArea makeTextArea(String placeholder) {
        JTextArea area = new JTextArea(placeholder);
        area.setBackground(Color.decode("#F8FAFC"));
        area.setForeground(Color.decode("#64748B"));
        area.setFont(new Font("Consolas", Font.PLAIN, 15));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        return area;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApplication app = new MainApplication();
            app.setVisible(true);
        });
    }

    private void printLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            consoleOutput.append(" > " + msg + "\n");
            consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
        });
    }

    private void startSystem() {
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            printLog("[ERROR] Dataset not found");
            return;
        }

        try {
            printLog("[INIT] Loading " + DATA_FILE);
            try (FileReader reader = new FileReader(dataFile)) {
                transactions = parser.parse(reader);
            }

            SecureFinancialLedger ledger = new SecureFinancialLedger(MASTER_KEY);
            for (FinancialTransaction tx : transactions) {
                try {
                    ledger.commitTransaction(tx);
                } catch (IllegalArgumentException ignored) {
                }
            }

            printLog("[INIT] Ledger ready");
            printLog("[SYSTEM] Starting background thread...");

            updateDisplay(true, "SECURE", "LEDGER: OK");

            AdversarialSimulationBot bot = new AdversarialSimulationBot(DATA_FILE, BACKUP_FILE);
            Thread botThread = new Thread(bot, "bot-thread");
            botThread.setDaemon(true);
            botThread.start();
            printLog("[SYSTEM] Bot started");

            updateTimer.start();

        } catch (Exception e) {
            printLog("Error: " + e.getMessage());
        }
    }

    private void runCheck() {
        passCount++;
        passCounter.setText("Pass: " + passCount);
        printLog(String.format("[CHECK #%d] Running...", passCount));
        File dataFile = new File(DATA_FILE);
        File backupFile = new File(BACKUP_FILE);
        try {
            boolean tampered = false;
            String reason = "Clean";
            synchronized (AdversarialSimulationBot.FILE_LOCK) {
                if (dataFile.length() != backupFile.length()) {
                    tampered = true;
                    reason = String.format("Size mismatch: %d vs %d bytes", dataFile.length(), backupFile.length());
                } else {
                    byte[] data = Files.readAllBytes(dataFile.toPath());
                    byte[] backup = Files.readAllBytes(backupFile.toPath());
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] != backup[i]) {
                            tampered = true;
                            reason = String.format("Diff at byte %d", i);
                            break;
                        }
                    }
                }
            }
            if (!tampered) {
                printLog(String.format("[CHECK #%d] OK", passCount));
                updateDisplay(true, "SECURE", "LEDGER: OK");
            } else {
                if (maintenanceMode) {
                    printLog(String.format("[MAINTENANCE #%d] Updating baseline", passCount));
                    synchronized (AdversarialSimulationBot.FILE_LOCK) {
                        try (FileReader reader = new FileReader(dataFile)) {
                            transactions = parser.parse(reader);
                        }
                        Files.copy(dataFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    printLog("[MAINTENANCE] Done");
                    updateDisplay(true, "MAINTAINED", "LEDGER: RE-SEALED");
                } else {
                    printLog(String.format("[BREACH #%d] DETECTED!", passCount));
                    updateDisplay(false, "BREACH", "LEDGER: BROKEN");
                    printLog("[FIX] Restoring file...");
                    synchronized (AdversarialSimulationBot.FILE_LOCK) {
                        Files.copy(backupFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        try (FileReader reader = new FileReader(dataFile)) {
                            transactions = parser.parse(reader);
                        }
                    }
                    new Timer(5000, e -> {
                        if (!isPaused) {
                            printLog("[FIX] Restored");
                            updateDisplay(true, "SECURE", "LEDGER: OK");
                            ((Timer) e.getSource()).stop();
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            printLog("Check error: " + e.getMessage());
        }
    }

    private void updateDisplay(boolean isOk, String status, String cryptoMsg) {
        SwingUtilities.invokeLater(() -> {
            statusValue.setText(status);
            cryptoStatus.setText(cryptoMsg);
            if (isOk) {
                if (maintenanceMode) {
                    statusPanel.setBackground(Color.decode("#EA580C"));
                    cryptoPanel.setBackground(Color.decode("#C2410C"));
                } else {
                    statusPanel.setBackground(Color.decode("#0284C7"));
                    cryptoPanel.setBackground(Color.decode("#059669"));
                }
                attackerInfo.setText("No attacks");
                attackerInfo.setForeground(Color.decode("#475569"));
            } else {
                statusPanel.setBackground(Color.decode("#DC2626"));
                cryptoPanel.setBackground(Color.decode("#991B1B"));
                String attack = SharedAttackState.getAttack();
                attackerInfo.setText(attack);
                attackerInfo.setForeground(Color.decode("#991B1B"));
                if (attackList.getText().contains("Queue")) {
                    attackList.setText("");
                }
                String shortAttack = attack.replace("Adversarial Bot ", "");
                attackList.append(String.format("[ATTACK #%d] %s%n", passCount, shortAttack));
                attackList.setForeground(Color.decode("#B91C1C"));
            }
        });
    }
}