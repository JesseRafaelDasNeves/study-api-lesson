package com.studygenie.lesson.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.studygenie.lesson.fileextraction.ExtractedTextResponse;
import com.studygenie.lesson.fileextraction.TextExtractionException;
import com.studygenie.lesson.fileextraction.TextExtractionService;
import com.studygenie.lesson.fileextraction.UnsupportedFileTypeException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller para extração de texto de arquivos.
 * Todos os processamentos ocorrem em memória — nenhum arquivo é persistido.
 *
 * <p>O texto extraído pode ser enviado diretamente para {@code POST /ai/summaries/by-text}
 * para geração de resumo com IA, sem intermediários no backend.
 */
@RestController
@RequestMapping("/files")
@Tag(
    name = "Extração de Texto de Arquivos",
    description = "Endpoint para extração de texto bruto a partir de arquivos PDF, DOCX ou TXT. "
                + "O arquivo é processado inteiramente em memória e descartado ao final da requisição — "
                + "nenhum dado é persistido no banco ou em disco."
)
public class FileExtractionController {

    @Autowired
    private TextExtractionService textExtractionService;

    @Operation(
        summary = "Extrair texto de um arquivo",
        description = "Recebe um arquivo via multipart/form-data e retorna o texto extraído para "
                    + "pré-visualização no frontend. "
                    + "Formatos aceitos: **PDF** (application/pdf), "
                    + "**DOCX** (application/vnd.openxmlformats-officedocument.wordprocessingml.document), "
                    + "**TXT** (text/plain). "
                    + "Tamanho máximo: configurável via `file-extraction.max-size-mb` (padrão 20 MB). "
                    + "O texto retornado pode ser enviado para `POST /ai/summaries/by-text`."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Texto extraído com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = ExtractedTextResponse.class))),
        @ApiResponse(responseCode = "400", description = "Arquivo vazio ou ausente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "413", description = "Arquivo excede o tamanho máximo permitido",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "415", description = "Tipo de arquivo não suportado (aceitos: PDF, DOCX, TXT)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "422", description = "Arquivo corrompido ou ilegível",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(value = "/extract-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExtractedTextResponse> extractText(
            @RequestParam("file") MultipartFile file) throws TextExtractionException {
        ExtractedTextResponse response = textExtractionService.extractFrom(file);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Tratamento de erros — corpo padronizado, sem stack trace
    // -------------------------------------------------------------------------

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedFileType(UnsupportedFileTypeException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(errorBody(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage()));
    }

    @ExceptionHandler(TextExtractionException.class)
    public ResponseEntity<Map<String, Object>> handleTextExtractionException(TextExtractionException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errorBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Captura o erro do Spring quando o arquivo excede spring.servlet.multipart.max-file-size,
     * padronizando o corpo de resposta sem expor detalhes internos.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(errorBody(HttpStatus.PAYLOAD_TOO_LARGE,
                        "O arquivo enviado excede o tamanho máximo permitido."));
    }

    private Map<String, Object> errorBody(HttpStatus status, String message) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}
