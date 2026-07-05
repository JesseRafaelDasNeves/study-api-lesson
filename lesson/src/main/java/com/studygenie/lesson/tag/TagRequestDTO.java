package com.studygenie.lesson.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequestDTO(

        @NotBlank(message = "Tag name is required") @Size(min = 3, max = 20, message = "Tag name must be between 3 and 20 characters") String name) {
    public Tag toTag() {
        Tag tag = new Tag();
        tag.setName(this.name);
        return tag;
    }

    public void mapToTag(Tag tag) {
        tag.setName(this.name);
    }
}
