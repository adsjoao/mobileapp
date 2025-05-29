package com.example.taskflow.data.entity;

import android.graphics.Color;

public enum TaskPriority {
    HIGH("Alta", Color.parseColor("#E53935")),
    MEDIUM("Média", Color.parseColor("#FF9800")),
    LOW("Baixa", Color.parseColor("#4CAF50"));

    private final String displayName;
    private final int color;

    TaskPriority(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }
}