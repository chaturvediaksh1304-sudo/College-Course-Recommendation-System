package com.example.coursereco.dto;

import java.util.Set;

public record AgentRecommendRequest(
        Long studentId,
        int limit,
        Set<String> extraInterests
) {}
