package com.studygenie.lesson.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studygenie.lesson.course.CourseRepository;
import com.studygenie.lesson.lesson.Lesson;
import com.studygenie.lesson.lesson.LessonRepository;
import com.studygenie.lesson.lesson.LessonRequestDTO;
import com.studygenie.lesson.lesson.LessonResponseDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/lessons")
public class LessonController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public List<LessonResponseDTO> getAll() {
        return lessonRepository.findAll().stream().map(LessonResponseDTO::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponseDTO> get(@PathVariable @NotNull UUID id) {
        return lessonRepository.findById(id)
                .map(lesson -> ResponseEntity.ok(LessonResponseDTO.from(lesson)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LessonResponseDTO> create(@RequestBody @Valid LessonRequestDTO dto) {
        return courseRepository.findById(dto.courseId())
                .map(course -> {
                    Lesson lesson = dto.toLesson(course);
                    lesson.setCreatedAt(LocalDateTime.now());
                    lesson.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(LessonResponseDTO.from(lessonRepository.save(lesson)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonResponseDTO> update(@PathVariable @NotNull UUID id,
            @RequestBody @Valid LessonRequestDTO dto) {
        return lessonRepository.findById(id)
                .map(lesson -> courseRepository.findById(dto.courseId())
                        .map(course -> {
                            dto.mapToLesson(lesson, course);
                            lesson.setUpdatedAt(LocalDateTime.now());
                            return ResponseEntity.ok(LessonResponseDTO.from(lessonRepository.save(lesson)));
                        })
                        .orElse(ResponseEntity.notFound().build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        if (lessonRepository.existsById(id)) {
            lessonRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
