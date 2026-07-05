package com.studygenie.lesson.fileextraction;

/**
 * DTO de resposta do endpoint POST /files/extract-text.
 * Contém o texto extraído do arquivo e metadados básicos do arquivo original.
 *
 * @param originalFileName nome do arquivo enviado pelo cliente
 * @param contentType      MIME type detectado/declarado do arquivo
 * @param extractedText    texto extraído do arquivo em formato plano
 * @param characterCount   número de caracteres do texto extraído
 */
public record ExtractedTextResponse(
        String originalFileName,
        String contentType,
        String extractedText,
        int characterCount
) {}
