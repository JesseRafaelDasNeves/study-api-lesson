package com.studygenie.lesson.summary;

import java.util.UUID;

import com.studygenie.lesson.tag.Tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TagRequestDTO(

        @NotNull(message = "Tag ID is required") UUID id,
        @NotBlank(message = "Tag name is required") @Size(min = 3, max = 20, message = "Tag name must be between 3 and 20 characters") String name) {
    public Tag toTag() {
        Tag tag = new Tag();
        tag.setName(this.name);
        tag.setId(this.id);
        return tag;
    }
}
