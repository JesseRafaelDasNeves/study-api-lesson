package com.studygenie.lesson.fileextraction.extractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.studygenie.lesson.fileextraction.TextExtractionException;

class PlainTextExtractorTest {

    private final PlainTextExtractor extractor = new PlainTextExtractor();

    // --- supports() ---

    @Test
    void supports_retornaTrue_paraTextPlain() {
        assertThat(extractor.supports("text/plain")).isTrue();
    }

    @Test
    void supports_retornaTrue_paraTextPlainComCharset() {
        assertThat(extractor.supports("text/plain; charset=UTF-8")).isTrue();
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
    void extract_retornaConteudo_paraArquivoTxtValido() throws Exception {
        String conteudo = "Texto de teste\nSegunda linha";
        InputStream stream = new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));

        String resultado = extractor.extract(stream);

        assertThat(resultado).isEqualTo(conteudo);
    }

    @Test
    void extract_retornaConteudo_comCaracteresEspeciais() throws Exception {
        String conteudo = "Ação, ênfase, coração ❤️";
        InputStream stream = new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));

        String resultado = extractor.extract(stream);

        assertThat(resultado).isEqualTo(conteudo);
    }

    @Test
    void extract_retornaStringVazia_paraArquivoVazio() throws Exception {
        InputStream stream = new ByteArrayInputStream(new byte[0]);

        String resultado = extractor.extract(stream);

        assertThat(resultado).isEmpty();
    }

    @Test
    void extract_lancaTextExtractionException_aoFalharLeitura() {
        InputStream streamQuebrado = new InputStream() {
            @Override
            public int read() throws java.io.IOException {
                throw new java.io.IOException("Erro simulado de I/O");
            }
        };

        assertThatThrownBy(() -> extractor.extract(streamQuebrado))
                .isInstanceOf(TextExtractionException.class)
                .hasMessageContaining("Falha ao ler o arquivo de texto");
    }
}
