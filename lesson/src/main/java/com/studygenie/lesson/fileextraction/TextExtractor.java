package com.studygenie.lesson.fileextraction;

import java.io.InputStream;

/**
 * Contrato para extratores de texto de arquivos.
 * Cada implementação suporta um content-type específico e é registrada
 * como @Component, permitindo extensão futura sem modificar o service.
 */
public interface TextExtractor {

    /**
     * Informa se este extrator é capaz de processar o content-type informado.
     *
     * @param contentType o MIME type do arquivo (ex: "application/pdf")
     * @return true se este extrator suporta o tipo
     */
    boolean supports(String contentType);

    /**
     * Extrai o texto do InputStream em memória.
     * O chamador é responsável por fechar o stream após a chamada.
     *
     * @param inputStream stream do arquivo recebido via MultipartFile
     * @return texto extraído sem formatação
     * @throws TextExtractionException se o conteúdo for ilegível ou corrompido
     */
    String extract(InputStream inputStream) throws TextExtractionException;
}
