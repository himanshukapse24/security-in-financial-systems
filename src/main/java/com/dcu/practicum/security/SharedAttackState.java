package com.dcu.practicum.security;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SharedAttackState {
    private static final AtomicInteger rowNum = new AtomicInteger(-1);
    private static final AtomicReference<String> colName = new AtomicReference<>("NONE");
    private static final AtomicReference<String> oldVal = new AtomicReference<>("");
    private static final AtomicReference<String> newVal = new AtomicReference<>("");

    public static String getAttack() {
        if (rowNum.get() == -1) {
            return "No attacks";
        }
        return String.format("Bot changed Row #%d | Column: [%s] | From '%s' to '%s'",
                rowNum.get(),
                colName.get(),
                oldVal.get(),
                newVal.get());
    }

    public static void setAttack(int row, String col, String old, String neu) {
        rowNum.set(row);
        colName.set(col);
        oldVal.set(old != null ? old : "NULL");
        newVal.set(neu != null ? neu : "NULL");
    }

}