package com.studygenie.lesson.fileextraction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.studygenie.lesson.fileextraction.extractor.PlainTextExtractor;

class TextExtractionServiceTest {

    private TextExtractionService service;
    private FileExtractionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileExtractionProperties(20); // limite de 20 MB
        PlainTextExtractor plainExtractor = new PlainTextExtractor();

        service = new TextExtractionService();
        // Injeção manual dos campos privados via reflexão (simulando Spring DI)
        injectField(service, "extractors", List.of(plainExtractor));
        injectField(service, "properties", properties);
    }

    // --- Seleção de extrator ---

    @Test
    void extractFrom_selecionaExtrator_comBaseNoContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.txt", "text/plain",
                "Conteúdo de teste".getBytes(StandardCharsets.UTF_8));

        ExtractedTextResponse response = service.extractFrom(file);

        assertThat(response.extractedText()).isEqualTo("Conteúdo de teste");
        assertThat(response.contentType()).isEqualTo("text/plain");
        assertThat(response.originalFileName()).isEqualTo("sample.txt");
        assertThat(response.characterCount()).isEqualTo("Conteúdo de teste".length());
    }

    @Test
    void extractFrom_selecionaExtrator_pelaExtensaoQuandoContentTypeNaoSuportado() throws Exception {
        // Simula upload com content-type genérico, mas extensão .txt
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.txt", "application/octet-stream",
                "Fallback pela extensão".getBytes(StandardCharsets.UTF_8));

        ExtractedTextResponse response = service.extractFrom(file);

        assertThat(response.extractedText()).isEqualTo("Fallback pela extensão");
    }

    // --- Exceções ---

    @Test
    void extractFrom_lancaUnsupportedFileTypeException_paraTipoNaoSuportado() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.extractFrom(file))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("image/png");
    }

    @Test
    void extractFrom_lancaIllegalArgumentException_paraArquivoVazio() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> service.extractFrom(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void extractFrom_lancaIllegalArgumentException_seExcederLimiteDeTamanho() {
        // Cria um properties com limite de 0 MB para forçar o erro
        FileExtractionProperties limiteZero = new FileExtractionProperties(1);
        injectField(service, "properties", limiteZero);

        // Arquivo de 2 MB
        byte[] dadosGrandes = new byte[2 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.txt", "text/plain", dadosGrandes);

        assertThatThrownBy(() -> service.extractFrom(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tamanho máximo");
    }

    @Test
    void extractFrom_lancaTextExtractionException_quandoExtratorFalha() throws Exception {
        // Cria um extrator mockado que sempre lança TextExtractionException
        TextExtractor extatorQuebrado = mock(TextExtractor.class);
        when(extatorQuebrado.supports("text/plain")).thenReturn(true);
        when(extatorQuebrado.extract(any())).thenThrow(
                new TextExtractionException("Erro simulado", new IOException("causa")));

        injectField(service, "extractors", List.of(extatorQuebrado));

        MockMultipartFile file = new MockMultipartFile(
                "file", "broken.txt", "text/plain",
                "conteúdo".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.extractFrom(file))
                .isInstanceOf(TextExtractionException.class)
                .hasMessageContaining("Erro simulado");
    }

    // -------------------------------------------------------------------------
    // Helper para injeção manual (evita Mockito InjectMocks com records e finals)
    // -------------------------------------------------------------------------

    private void injectField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Falha na injeção do campo: " + fieldName, e);
        }
    }
}
