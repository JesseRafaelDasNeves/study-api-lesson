package com.studygenie.lesson.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateSummaryFromTextRequest(
        @NotBlank(message = "O conteúdo do texto é obrigatório")
        @Size(max = 50000, message = "O texto não pode exceder 50.000 caracteres")
        String text
) {}
