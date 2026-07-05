package com.studygenie.lesson.summary;

import java.util.List;
import java.util.UUID;

import com.studygenie.lesson.lesson.Lesson;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SummaryRequestDTO(

        @NotNull(message = "Lesson ID is required") UUID lessonId,

        @NotBlank(message = "Summary content is required") String content,

        @NotNull(message = "Summary source is required") SummarySource source,

        @Valid List<TagRequestDTO> tags) {

    public Summary toSummary(Lesson lesson) {
        Summary summary = new Summary();
        summary.setLesson(lesson);
        summary.setContent(this.content);
        summary.setSource(this.source);
        return summary;
    }

    public void mapToSummary(Summary summary, Lesson lesson) {
        summary.setLesson(lesson);
        summary.setContent(this.content);
        summary.setSource(this.source);
    }
}
