package com.studygenie.lesson.summary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SummarySearchResultItem(
        UUID id,
        String title,
        String contentSnippet,
        UUID lessonId,
        String lessonTitle,
        UUID courseId,
        String courseName,
        List<String> tags,
        Instant createdAt,
        Double relevanceScore
) {}
