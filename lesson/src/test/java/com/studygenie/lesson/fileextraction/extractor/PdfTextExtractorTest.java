package com.studygenie.lesson.fileextraction.extractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import com.studygenie.lesson.fileextraction.TextExtractionException;

class PdfTextExtractorTest {

    private final PdfTextExtractor extractor = new PdfTextExtractor();

    // --- supports() ---

    @Test
    void supports_retornaTrue_paraApplicationPdf() {
        assertThat(extractor.supports("application/pdf")).isTrue();
    }

    @Test
    void supports_retornaFalse_paraTextPlain() {
        assertThat(extractor.supports("text/plain")).isFalse();
    }

    @Test
    void supports_retornaFalse_paraNull() {
        assertThat(extractor.supports(null)).isFalse();
    }

    // --- extract() ---

    @Test
    void extract_retornaTexto_paraPdfValido() throws Exception {
        byte[] pdfBytes = criarPdfEmMemoria("Texto de teste em PDF");

        String resultado = extractor.extract(new ByteArrayInputStream(pdfBytes));

        assertThat(resultado).contains("Texto de teste em PDF");
    }

    @Test
    void extract_lancaTextExtractionException_paraBytesInvalidos() {
        byte[] bytesInvalidos = "isto nao e um pdf valido".getBytes();

        assertThatThrownBy(() -> extractor.extract(new ByteArrayInputStream(bytesInvalidos)))
                .isInstanceOf(TextExtractionException.class)
                .hasMessageContaining("Falha ao processar o arquivo PDF");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Cria um PDF mínimo em memória com o texto informado, usando PDFBox 3.x.
     */
    private byte[] criarPdfEmMemoria(String texto) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(50, 700);
                content.showText(texto);
                content.endText();
            }

            document.save(out);
            return out.toByteArray();
        }
    }
}
