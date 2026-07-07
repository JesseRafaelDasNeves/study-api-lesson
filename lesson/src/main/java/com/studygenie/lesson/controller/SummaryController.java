package com.studygenie.lesson.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.studygenie.lesson.lesson.LessonRepository;
import com.studygenie.lesson.summary.Summary;
import com.studygenie.lesson.summary.SummaryRepository;
import com.studygenie.lesson.summary.SummaryRequestDTO;
import com.studygenie.lesson.summary.SummaryResponseDTO;
import com.studygenie.lesson.summary.SummaryTag;
import com.studygenie.lesson.summary.SummaryTagRepository;
import com.studygenie.lesson.tag.Tag;
import com.studygenie.lesson.tag.TagRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.studygenie.lesson.summary.SummarySearchCriteria;
import com.studygenie.lesson.summary.SummarySearchResultItem;
import com.studygenie.lesson.summary.SummarySearchService;
import com.studygenie.lesson.summary.SummarySortBy;
import com.studygenie.lesson.summary.SummarySortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/summaries")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Resumos", description = "Operações de CRUD para gerenciamento de resumos vinculados a aulas e tags.")
public class SummaryController {

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private SummaryTagRepository summaryTagRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SummarySearchService summarySearchService;

    @Operation(summary = "Buscar resumos de forma paginada", description = "Busca resumos por texto completo, filtros opcionais por curso, aula e tags, com ordenação flexível.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos de ordenação ou paginação")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<SummarySearchResultItem>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID lessonId,
            @RequestParam(required = false) List<UUID> tagIds,
            @RequestParam(defaultValue = "DATE") SummarySortBy sortBy,
            @RequestParam(defaultValue = "DESC") SummarySortDirection sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100; // Limite razoável para evitar abuso
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }

        SummarySearchCriteria criteria = new SummarySearchCriteria(
                query, courseId, lessonId, tagIds, sortBy, sortDirection
        );

        Pageable pageable = PageRequest.of(page, size);
        try {
            Page<SummarySearchResultItem> result = summarySearchService.search(criteria, pageable);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Listar todos os resumos", description = "Retorna a lista completa de resumos cadastrados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public List<SummaryResponseDTO> getAll() {
        return summaryRepository.findAll().stream().map(SummaryResponseDTO::from).toList();
    }

    @Operation(summary = "Buscar resumo por ID", description = "Retorna um resumo específico pelo seu UUID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumo encontrado"),
        @ApiResponse(responseCode = "404", description = "Resumo não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SummaryResponseDTO> get(@PathVariable @NotNull UUID id) {
        return summaryRepository.findById(id)
                .map(summary -> ResponseEntity.ok(SummaryResponseDTO.from(summary)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar resumo", description = "Cria um novo resumo vinculado a uma aula. Opcionalmente associa tags existentes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumo criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou tag referenciada não encontrada"),
        @ApiResponse(responseCode = "404", description = "Aula referenciada não encontrada")
    })
    @PostMapping
    public ResponseEntity<SummaryResponseDTO> create(@RequestBody @Valid SummaryRequestDTO dto) {
        return lessonRepository.findById(dto.lessonId())
                .map(lesson -> {
                    Summary summary = dto.toSummary(lesson);
                    summary.setCreatedAt(LocalDateTime.now());
                    summary.setUpdatedAt(LocalDateTime.now());
                    Summary savedSummary = summaryRepository.save(summary);

                    if (dto.tags() != null && !dto.tags().isEmpty()) {
                        List<SummaryTag> summaryTags = new ArrayList<>();
                        for (var tagDTO : dto.tags()) {
                            Tag tag = tagRepository.findById(tagDTO.id()).orElse(null);
                            if (tag == null) {
                                return ResponseEntity.badRequest().<SummaryResponseDTO>build();
                            }
                            SummaryTag summaryTag = new SummaryTag();
                            summaryTag.setSummary(savedSummary);
                            summaryTag.setTag(tag);
                            summaryTag.setCreatedAt(LocalDateTime.now());
                            summaryTag.setUpdatedAt(LocalDateTime.now());
                            summaryTags.add(summaryTag);
                        }
                        summaryTagRepository.saveAll(summaryTags);
                        savedSummary.setSummaryTags(summaryTags);
                    }

                    return ResponseEntity.ok(SummaryResponseDTO.from(savedSummary));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar resumo", description = "Atualiza os dados de um resumo existente. As tags são substituídas pelas informadas no payload.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumo atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou tag referenciada não encontrada"),
        @ApiResponse(responseCode = "404", description = "Resumo ou aula não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SummaryResponseDTO> update(@PathVariable @NotNull UUID id,
            @RequestBody @Valid SummaryRequestDTO dto) {
        return summaryRepository.findById(id)
                .map(summary -> lessonRepository.findById(dto.lessonId())
                        .map(lesson -> {
                            dto.mapToSummary(summary, lesson);
                            summary.setUpdatedAt(LocalDateTime.now());

                            summaryTagRepository.deleteBySummary(summary);
                            summary.getSummaryTags().clear();

                            Summary updatedSummary = summaryRepository.save(summary);

                            if (dto.tags() != null && !dto.tags().isEmpty()) {
                                List<SummaryTag> summaryTags = new ArrayList<>();
                                for (var tagDTO : dto.tags()) {
                                    Tag tag = tagRepository.findById(tagDTO.id()).orElse(null);
                                    if (tag == null) {
                                        return ResponseEntity.badRequest().<SummaryResponseDTO>build();
                                    }
                                    SummaryTag summaryTag = new SummaryTag();
                                    summaryTag.setSummary(updatedSummary);
                                    summaryTag.setTag(tag);
                                    summaryTag.setCreatedAt(LocalDateTime.now());
                                    summaryTag.setUpdatedAt(LocalDateTime.now());
                                    summaryTags.add(summaryTag);
                                }
                                summaryTagRepository.saveAll(summaryTags);
                                updatedSummary.setSummaryTags(summaryTags);
                            }

                            return ResponseEntity.ok(SummaryResponseDTO.from(updatedSummary));
                        })
                        .orElse(ResponseEntity.notFound().build()))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        return summaryRepository.findById(id)
                .map(summary -> {
                    if (summary.getLesson() != null) {
                        summary.getLesson().setSummary(null);
                    }
                    summaryRepository.delete(summary);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
