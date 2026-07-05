package com.studygenie.lesson.ai;

import java.util.List;

public record GeneratedSummaryResponse(
        String suggestedTitle,
        String content,
        List<String> suggestedTags
) {}
