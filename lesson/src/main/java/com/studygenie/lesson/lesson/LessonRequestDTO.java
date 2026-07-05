package com.studygenie.lesson.lesson;

import java.time.LocalDateTime;
import java.util.UUID;

import com.studygenie.lesson.course.Course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonRequestDTO(

        @NotNull(message = "Course ID is required") UUID courseId,

        @NotBlank(message = "Lesson title is required") @Size(min = 3, max = 100, message = "Lesson title must be between 3 and 100 characters") String title,

        String description,

        @NotNull(message = "Lesson date is required") LocalDateTime date) {

    public Lesson toLesson(Course course) {
        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(this.title);
        lesson.setDescription(this.description);
        lesson.setDate(this.date);
        return lesson;
    }

    public void mapToLesson(Lesson lesson, Course course) {
        lesson.setCourse(course);
        lesson.setTitle(this.title);
        lesson.setDescription(this.description);
        lesson.setDate(this.date);
    }
}
