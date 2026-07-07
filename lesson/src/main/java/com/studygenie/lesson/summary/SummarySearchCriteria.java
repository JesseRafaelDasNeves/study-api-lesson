package com.studygenie.lesson.summary;

import java.util.List;
import java.util.UUID;

public record SummarySearchCriteria(
        String query,
        UUID courseId,
        UUID lessonId,
        List<UUID> tagIds,
        SummarySortBy sortBy,
        SummarySortDirection sortDirection
) {}
