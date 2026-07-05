package com.studygenie.lesson.tag;

import java.util.UUID;

public record TagResponseDTO(UUID id, String name) {
    public static TagResponseDTO from(Tag tag) {
        return new TagResponseDTO(tag.getId(), tag.getName());
    }
}
