package com.studygenie.lesson.course;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseResponseDTO(
        UUID id,
        String name,
        String acronym,
        String color,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseResponseDTO from(Course course) {
        return new CourseResponseDTO(
                course.getId(),
                course.getName(),
                course.getAcronym(),
                course.getColor(),
                course.getDescription(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
