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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/summaries")
public class SummaryController {

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private SummaryTagRepository summaryTagRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public List<SummaryResponseDTO> getAll() {
        return summaryRepository.findAll().stream().map(SummaryResponseDTO::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SummaryResponseDTO> get(@PathVariable @NotNull UUID id) {
        return summaryRepository.findById(id)
                .map(summary -> ResponseEntity.ok(SummaryResponseDTO.from(summary)))
                .orElse(ResponseEntity.notFound().build());
    }

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
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        if (summaryRepository.existsById(id)) {
            summaryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
