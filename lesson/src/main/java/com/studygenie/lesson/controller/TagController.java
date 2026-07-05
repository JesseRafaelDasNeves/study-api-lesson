package com.studygenie.lesson.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.studygenie.lesson.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import com.studygenie.lesson.tag.TagRepository;
import com.studygenie.lesson.tag.TagRequestDTO;
import com.studygenie.lesson.tag.TagResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Operações de CRUD para gerenciamento de tags de categorização de resumos.")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @Operation(summary = "Listar todas as tags", description = "Retorna a lista completa de tags cadastradas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public List<TagResponseDTO> getAll() {
        return tagRepository.findAll().stream().map(TagResponseDTO::from).toList();
    }

    @Operation(summary = "Buscar tag por ID", description = "Retorna uma tag específica pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag encontrada"),
        @ApiResponse(responseCode = "404", description = "Tag não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDTO> get(@PathVariable @NotNull UUID id) {
        return tagRepository.findById(id).map(tag -> ResponseEntity.ok(TagResponseDTO.from(tag)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar tag", description = "Cria uma nova tag com os dados fornecidos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição")
    })
    @PostMapping()
    public ResponseEntity<TagResponseDTO> create(@RequestBody @Valid TagRequestDTO dto) {
        Tag tag = dto.toTag();
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(TagResponseDTO.from(tagRepository.save(tag)));
    }

    @Operation(summary = "Atualizar tag", description = "Atualiza os dados de uma tag existente pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos no corpo da requisição"),
        @ApiResponse(responseCode = "404", description = "Tag não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDTO> update(@PathVariable @NotNull UUID id,
            @RequestBody @Valid TagRequestDTO dto) {
        return tagRepository.findById(id).map(tag -> {
            dto.mapToTag(tag);
            tag.setUpdatedAt(LocalDateTime.now());
            Tag updatedTag = tagRepository.save(tag);
            return ResponseEntity.ok(TagResponseDTO.from(updatedTag));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Excluir tag", description = "Remove uma tag do sistema pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tag excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tag não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
