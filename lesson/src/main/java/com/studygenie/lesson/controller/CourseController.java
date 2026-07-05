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

import com.studygenie.lesson.course.Course;
import com.studygenie.lesson.course.CourseRepository;
import com.studygenie.lesson.course.CourseRequestDTO;
import com.studygenie.lesson.course.CourseResponseDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public List<CourseResponseDTO> getAll() {
        return courseRepository.findAll().stream().map(CourseResponseDTO::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> get(@PathVariable @NotNull UUID id) {
        return courseRepository.findById(id).map(course -> ResponseEntity.ok(CourseResponseDTO.from(course)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourseResponseDTO> create(@RequestBody @Valid CourseRequestDTO dto) {
        Course course = dto.toCourse();
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(CourseResponseDTO.from(courseRepository.save(course)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> update(@PathVariable @NotNull UUID id,
            @RequestBody @Valid CourseRequestDTO dto) {
        return courseRepository.findById(id).map(course -> {
            dto.mapToCourse(course);
            course.setUpdatedAt(LocalDateTime.now());
            Course updatedCourse = courseRepository.save(course);
            return ResponseEntity.ok(CourseResponseDTO.from(updatedCourse));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
