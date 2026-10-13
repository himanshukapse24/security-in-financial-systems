package com.dcu.practicum.analytics;

public record ModelEvaluationMetrics(
        int truePositives,
        int falsePositives,
        int trueNegatives,
        int falseNegatives,
        double precision,
        double recall,
        double f1Score
) {
}