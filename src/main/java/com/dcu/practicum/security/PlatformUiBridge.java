package com.dcu.practicum.security;

import java.util.concurrent.atomic.AtomicReference;

public class PlatformUiBridge {
    private static final AtomicReference<String> botMsg = new AtomicReference<>("Waiting...");
    private static final AtomicReference<String> forensicMsg = new AtomicReference<>("System OK");

    public static void pushInfo(String botLog, String forensic) {
        botMsg.set(botLog);
        forensicMsg.set(forensic);
    }

    public static String getBotMsg() {
        return botMsg.get();
    }

    public static String getForensicMsg() {
        return forensicMsg.get();
    }
}