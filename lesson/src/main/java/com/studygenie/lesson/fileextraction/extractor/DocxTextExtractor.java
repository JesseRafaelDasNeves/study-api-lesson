package com.studygenie.lesson.fileextraction.extractor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import com.studygenie.lesson.fileextraction.TextExtractionException;
import com.studygenie.lesson.fileextraction.TextExtractor;

/**
 * Extrator de texto para arquivos DOCX (Word Open XML).
 * Utiliza Apache POI XWPFDocument, processando inteiramente em memória.
 */
@Component
public class DocxTextExtractor implements TextExtractor {

    private static final String SUPPORTED_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Override
    public boolean supports(String contentType) {
        return SUPPORTED_CONTENT_TYPE.equalsIgnoreCase(contentType);
    }

    /**
     * Extrai o texto de todos os parágrafos do documento DOCX.
     * Tabelas e cabeçalhos/rodapés não são incluídos (melhoria futura).
     *
     * @param inputStream stream do arquivo DOCX recebido via MultipartFile
     * @return texto plano com parágrafos separados por quebra de linha
     * @throws TextExtractionException se o DOCX estiver corrompido ou ilegível
     */
    @Override
    public String extract(InputStream inputStream) throws TextExtractionException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            return sb.toString().trim();
        } catch (IOException | RuntimeException e) {
            throw new TextExtractionException(
                    "Falha ao processar o arquivo DOCX. Verifique se o arquivo não está corrompido.", e);
        }
    }
}
