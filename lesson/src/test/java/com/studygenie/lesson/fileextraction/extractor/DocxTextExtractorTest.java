package com.studygenie.lesson.fileextraction.extractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import com.studygenie.lesson.fileextraction.TextExtractionException;

class DocxTextExtractorTest {

    private final DocxTextExtractor extractor = new DocxTextExtractor();

    // --- supports() ---

    @Test
    void supports_retornaTrue_paraDocxContentType() {
        assertThat(extractor.supports(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document")).isTrue();
    }

    @Test
    void supports_retornaFalse_paraApplicationPdf() {
        assertThat(extractor.supports("application/pdf")).isFalse();
    }

    @Test
    void supports_retornaFalse_paraNull() {
        assertThat(extractor.supports(null)).isFalse();
    }

    // --- extract() ---

    @Test
    void extract_retornaTexto_paraDocxValido() throws Exception {
        byte[] docxBytes = criarDocxEmMemoria("Parágrafo de teste no DOCX");

        String resultado = extractor.extract(new ByteArrayInputStream(docxBytes));

        assertThat(resultado).contains("Parágrafo de teste no DOCX");
    }

    @Test
    void extract_retornaStringVazia_paraDocxSemParagrafos() throws Exception {
        byte[] docxBytes = criarDocxVazioEmMemoria();

        String resultado = extractor.extract(new ByteArrayInputStream(docxBytes));

        assertThat(resultado).isEmpty();
    }

    @Test
    void extract_lancaTextExtractionException_paraBytesInvalidos() {
        byte[] bytesInvalidos = "nao e um docx valido".getBytes();

        assertThatThrownBy(() -> extractor.extract(new ByteArrayInputStream(bytesInvalidos)))
                .isInstanceOf(TextExtractionException.class)
                .hasMessageContaining("Falha ao processar o arquivo DOCX");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Cria um DOCX mínimo em memória com um parágrafo de texto.
     */
    private byte[] criarDocxEmMemoria(String texto) throws Exception {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(texto);

            document.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Cria um DOCX mínimo em memória sem parágrafos com texto.
     */
    private byte[] criarDocxVazioEmMemoria() throws Exception {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.write(out);
            return out.toByteArray();
        }
    }
}
