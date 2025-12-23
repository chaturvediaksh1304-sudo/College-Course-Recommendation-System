package com.example.coursereco.dto;

public record AgentRecommendation(
        String courseCode,
        String title,
        int credits,
        double score,
        String agentReason,
        String whyFitMajor,
        String whyFitInterests
) {}
