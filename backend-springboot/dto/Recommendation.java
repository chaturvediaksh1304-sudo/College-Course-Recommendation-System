package com.example.coursereco.dto;

public record Recommendation(
        String courseCode,
        String title,
        int credits,
        double score,
        String reason
) {}
