package com.studygenie.lesson.fileextraction;

/**
 * Exceção lançada quando nenhum extrator registrado suporta o content-type
 * do arquivo enviado. Mapeia para HTTP 415 Unsupported Media Type.
 */
public class UnsupportedFileTypeException extends RuntimeException {

    private final String contentType;

    public UnsupportedFileTypeException(String contentType) {
        super("Tipo de arquivo não suportado: " + contentType
              + ". Tipos aceitos: application/pdf, "
              + "application/vnd.openxmlformats-officedocument.wordprocessingml.document, "
              + "text/plain.");
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
