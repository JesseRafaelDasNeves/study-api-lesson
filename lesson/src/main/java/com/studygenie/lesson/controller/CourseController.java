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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/courses")
@Tag(name = "Cursos", description = "Operações de CRUD para gerenciamento de cursos.")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Operation(summary = "Listar todos os cursos", description = "Retorna a lista completa de cursos cadastrados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public List<CourseResponseDTO> getAll() {
        return courseRepository.findAll().stream().map(CourseResponseDTO::from).toList();
    }

    @Operation(summary = "Buscar curso por ID", description = "Retorna um curso específico pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Curso encontrado"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> get(@PathVariable @NotNull UUID id) {
        return courseRepository.findById(id).map(course -> ResponseEntity.ok(CourseResponseDTO.from(course)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar curso", description = "Cria um novo curso com os dados fornecidos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Curso criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição")
    })
    @PostMapping
    public ResponseEntity<CourseResponseDTO> create(@RequestBody @Valid CourseRequestDTO dto) {
        Course course = dto.toCourse();
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(CourseResponseDTO.from(courseRepository.save(course)));
    }

    @Operation(summary = "Atualizar curso", description = "Atualiza os dados de um curso existente pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Curso atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
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

    @Operation(summary = "Excluir curso", description = "Remove um curso do sistema pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Curso excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
