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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public List<TagResponseDTO> getAll() {
        return tagRepository.findAll().stream().map(TagResponseDTO::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDTO> get(@PathVariable @NotNull UUID id) {
        return tagRepository.findById(id).map(tag -> ResponseEntity.ok(TagResponseDTO.from(tag)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<TagResponseDTO> create(@RequestBody @Valid TagRequestDTO dto) {
        Tag tag = dto.toTag();
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(TagResponseDTO.from(tagRepository.save(tag)));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull UUID id) {
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
