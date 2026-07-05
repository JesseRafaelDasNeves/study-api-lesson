package com.studygenie.lesson.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateSummaryFromTopicRequest(
        @NotBlank(message = "O tópico é obrigatório")
        @Size(max = 300, message = "O tópico não pode exceder 300 caracteres")
        String topic
) {}
