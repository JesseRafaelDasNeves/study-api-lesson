package com.studygenie.lesson.summary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.studygenie.lesson.lesson.LessonResponseDTO;
import com.studygenie.lesson.tag.TagResponseDTO;

public record SummaryResponseDTO(
        UUID id,
        LessonResponseDTO lesson,
        String content,
        SummarySource source,
        List<TagResponseDTO> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static SummaryResponseDTO from(Summary summary) {
        List<TagResponseDTO> tags = summary.getSummaryTags().stream()
                .map(summaryTag -> TagResponseDTO.from(summaryTag.getTag()))
                .toList();

        return new SummaryResponseDTO(
                summary.getId(),
                summary.getLesson() != null ? LessonResponseDTO.from(summary.getLesson()) : null,
                summary.getContent(),
                summary.getSource(),
                tags,
                summary.getCreatedAt(),
                summary.getUpdatedAt());
    }
}
