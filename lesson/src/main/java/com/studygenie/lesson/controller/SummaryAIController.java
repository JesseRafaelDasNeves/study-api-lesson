package com.studygenie.lesson.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studygenie.lesson.ai.GenerateSummaryFromTextRequest;
import com.studygenie.lesson.ai.GenerateSummaryFromTopicRequest;
import com.studygenie.lesson.ai.GeneratedSummaryResponse;
import com.studygenie.lesson.ai.SummaryAIService;
import com.studygenie.lesson.contentgenerator.ContentGeneratorException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/ai/summaries")
@Tag(name = "Resumos com IA", description = "Endpoints para geração de resumos usando o Content Generator (proxy para LLM). Nenhuma persistência ocorre aqui — o usuário salva o resultado via POST /summaries após confirmar.")
public class SummaryAIController {

    @Autowired
    private SummaryAIService summaryAIService;

    @Operation(
            summary = "Gerar resumo a partir de texto extraído",
            description = "Recebe um texto já extraído de um arquivo (PDF, DOCX etc.) e retorna um resumo gerado " +
                          "em português pela IA, com título sugerido. Não persiste nada no banco de dados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (texto vazio ou excede o limite)"),
            @ApiResponse(responseCode = "502", description = "Falha na comunicação com o serviço de IA")
    })
    @PostMapping("/by-text")
    public ResponseEntity<GeneratedSummaryResponse> generateFromText(
            @RequestBody @Valid GenerateSummaryFromTextRequest request) {
        GeneratedSummaryResponse response = summaryAIService.generateFromText(request.text());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Gerar resumo a partir de um tópico",
            description = "Recebe um tópico ou tema informado pelo usuário e retorna um resumo didático " +
                          "gerado em português pela IA, com título sugerido. Não persiste nada no banco de dados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (tópico vazio ou excede o limite)"),
            @ApiResponse(responseCode = "502", description = "Falha na comunicação com o serviço de IA")
    })
    @PostMapping("/by-topic")
    public ResponseEntity<GeneratedSummaryResponse> generateFromTopic(
            @RequestBody @Valid GenerateSummaryFromTopicRequest request) {
        GeneratedSummaryResponse response = summaryAIService.generateFromTopic(request.topic());
        return ResponseEntity.ok(response);
    }

    /**
     * Mapeia ContentGeneratorException para 502 Bad Gateway.
     * A mensagem é genérica e não expõe detalhes internos da chamada HTTP.
     */
    @ExceptionHandler(ContentGeneratorException.class)
    public ResponseEntity<Map<String, Object>> handleContentGeneratorException(ContentGeneratorException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_GATEWAY.value(),
                "error", "Bad Gateway",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}
