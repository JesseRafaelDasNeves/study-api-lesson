package com.studygenie.lesson.lesson;

import java.time.LocalDateTime;
import java.util.UUID;
import com.studygenie.lesson.course.CourseResponseDTO;

public record LessonResponseDTO(
        UUID id,
        CourseResponseDTO course,
        String title,
        String description,
        LocalDateTime date,
        Boolean hasSummary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static LessonResponseDTO from(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getCourse() != null ? CourseResponseDTO.from(lesson.getCourse()) : null,
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getDate(),
                lesson.getSummary() != null,
                lesson.getCreatedAt(),
                lesson.getUpdatedAt());
    }
}
