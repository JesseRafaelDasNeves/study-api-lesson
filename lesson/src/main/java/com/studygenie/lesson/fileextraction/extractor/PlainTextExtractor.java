package com.studygenie.lesson.fileextraction.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.studygenie.lesson.fileextraction.TextExtractionException;
import com.studygenie.lesson.fileextraction.TextExtractor;

/**
 * Extrator de texto para arquivos de texto plano (text/plain).
 * Lê o InputStream diretamente como UTF-8, sem dependências externas.
 */
@Component
public class PlainTextExtractor implements TextExtractor {

    private static final String SUPPORTED_CONTENT_TYPE = "text/plain";

    @Override
    public boolean supports(String contentType) {
        // Aceita também variantes com charset, ex: "text/plain; charset=UTF-8"
        return contentType != null && contentType.toLowerCase().startsWith(SUPPORTED_CONTENT_TYPE);
    }

    /**
     * Lê todos os bytes do InputStream e converte para String UTF-8.
     *
     * @param inputStream stream do arquivo TXT recebido via MultipartFile
     * @return conteúdo do arquivo como String
     * @throws TextExtractionException em caso de falha de I/O (raro para texto plano)
     */
    @Override
    public String extract(InputStream inputStream) throws TextExtractionException {
        try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TextExtractionException(
                    "Falha ao ler o arquivo de texto. Verifique se o arquivo é válido.", e);
        }
    }
}
