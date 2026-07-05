package com.studygenie.lesson.lesson;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonResponseDTO(
        UUID id,
        UUID courseId,
        String title,
        String description,
        LocalDateTime date,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static LessonResponseDTO from(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getCourse().getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getDate(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt());
    }
}
