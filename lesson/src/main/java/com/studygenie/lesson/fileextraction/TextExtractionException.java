package com.studygenie.lesson.fileextraction;

/**
 * Exceção lançada quando o extrator falha ao processar o conteúdo do arquivo
 * (ex: PDF corrompido, DOCX mal-formado). Mapeia para HTTP 422.
 */
public class TextExtractionException extends Exception {

    public TextExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
