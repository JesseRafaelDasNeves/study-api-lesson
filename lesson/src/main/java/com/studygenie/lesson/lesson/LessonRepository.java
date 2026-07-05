package com.studygenie.lesson.lesson;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
}
