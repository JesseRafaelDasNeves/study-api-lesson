package com.studygenie.lesson.fileextraction.extractor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import com.studygenie.lesson.fileextraction.TextExtractionException;
import com.studygenie.lesson.fileextraction.TextExtractor;

/**
 * Extrator de texto para arquivos PDF.
 * Utiliza PDFBox 3.x (API Loader.loadPDF), processando o documento inteiramente
 * em memória — nenhum arquivo temporário é criado em disco.
 */
@Component
public class PdfTextExtractor implements TextExtractor {

    private static final String SUPPORTED_CONTENT_TYPE = "application/pdf";

    @Override
    public boolean supports(String contentType) {
        return SUPPORTED_CONTENT_TYPE.equalsIgnoreCase(contentType);
    }

    /**
     * Extrai todo o texto do PDF em memória usando PDFTextStripper.
     *
     * @param inputStream stream do arquivo PDF recebido via MultipartFile
     * @return texto plano extraído de todas as páginas
     * @throws TextExtractionException se o PDF estiver corrompido ou ilegível
     */
    @Override
    public String extract(InputStream inputStream) throws TextExtractionException {
        try {
            // PDFBox 3.x: Loader.loadPDF() substitui PDDocument.load()
            // readAllBytes garante que o stream é lido antes do PDDocument ser fechado
            byte[] bytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException e) {
            throw new TextExtractionException(
                    "Falha ao processar o arquivo PDF. Verifique se o arquivo não está corrompido.", e);
        }
    }
}
