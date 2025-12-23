package com.example.coursereco.dto;

import java.util.Set;

public record RecommendRequest(
        Long studentId,
        int limit,
        Set<String> extraInterests
) {}
