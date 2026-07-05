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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/lessons")
@Tag(name = "Aulas", description = "Operações de CRUD para gerenciamento de aulas vinculadas a cursos.")
public class LessonController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Operation(summary = "Listar todas as aulas", description = "Retorna a lista completa de aulas cadastradas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public List<LessonResponseDTO> getAll() {
        return lessonRepository.findAll().stream().map(LessonResponseDTO::from).toList();
    }

    @Operation(summary = "Buscar aula por ID", description = "Retorna uma aula específica pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aula encontrada"),
        @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponseDTO> get(@PathVariable @NotNull UUID id) {
        return lessonRepository.findById(id)
                .map(lesson -> ResponseEntity.ok(LessonResponseDTO.from(lesson)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar aula", description = "Cria uma nova aula vinculada a um curso existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aula criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição"),
        @ApiResponse(responseCode = "404", description = "Curso referenciado não encontrado")
    })
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

    @Operation(summary = "Atualizar aula", description = "Atualiza os dados de uma aula existente pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aula atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição"),
        @ApiResponse(responseCode = "404", description = "Aula ou curso não encontrado")
    })
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

    @Operation(summary = "Excluir aula", description = "Remove uma aula do sistema pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Aula excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        if (lessonRepository.existsById(id)) {
            lessonRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
