package com.studygenie.lesson.fileextraction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.studygenie.lesson.controller.FileExtractionController;

@WebMvcTest(FileExtractionController.class)
class FileExtractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TextExtractionService textExtractionService;

    // --- Caminho feliz ---

    @Test
    void extractText_retorna200_comRespostaCorreta_paraTxtValido() throws Exception {
        ExtractedTextResponse mockResponse = new ExtractedTextResponse(
                "sample.txt", "text/plain", "Texto extraído", 14);

        when(textExtractionService.extractFrom(any())).thenReturn(mockResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.txt", "text/plain",
                "Texto extraído".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/files/extract-text").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value("sample.txt"))
                .andExpect(jsonPath("$.contentType").value("text/plain"))
                .andExpect(jsonPath("$.extractedText").value("Texto extraído"))
                .andExpect(jsonPath("$.characterCount").value(14));
    }

    // --- Tipo não suportado (415) ---

    @Test
    void extractText_retorna415_paraTipoNaoSuportado() throws Exception {
        when(textExtractionService.extractFrom(any()))
                .thenThrow(new UnsupportedFileTypeException("image/png"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "imagem.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/files/extract-text").file(file))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Arquivo corrompido (422) ---

    @Test
    void extractText_retorna422_paraArquivoCorompido() throws Exception {
        when(textExtractionService.extractFrom(any()))
                .thenThrow(new TextExtractionException("PDF corrompido", new Exception("causa")));

        MockMultipartFile file = new MockMultipartFile(
                "file", "corrompido.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/files/extract-text").file(file))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    // --- Arquivo vazio (400) ---

    @Test
    void extractText_retorna400_paraArquivoVazio() throws Exception {
        when(textExtractionService.extractFrom(any()))
                .thenThrow(new IllegalArgumentException("O arquivo enviado está vazio."));

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/files/extract-text").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("O arquivo enviado está vazio."));
    }
}
