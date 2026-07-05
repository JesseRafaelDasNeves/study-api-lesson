package com.studygenie.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studygenie.lesson.ai.GeneratedSummaryResponse;
import com.studygenie.lesson.ai.SummaryAIService;
import com.studygenie.lesson.ai.SummaryPromptBuilder;
import com.studygenie.lesson.contentgenerator.ContentGeneratorClient;
import com.studygenie.lesson.contentgenerator.ContentGeneratorException;

@ExtendWith(MockitoExtension.class)
class SummaryAIServiceTest {

    @Mock
    private ContentGeneratorClient contentGeneratorClient;

    @InjectMocks
    private SummaryAIService summaryAIService;

    // -------------------------------------------------------------------------
    // generateFromText
    // -------------------------------------------------------------------------

    @Test
    void generateFromText_deveEnviarPromptCorretoAoClient() {
        String inputText = "Algoritmos de ordenação são métodos para organizar elementos em uma sequência.";
        String iaResponse = "Resumo gerado: algoritmos organizam dados.\nSegunda linha.";
        String expectedPrompt = SummaryPromptBuilder.buildFromText(inputText);

        when(contentGeneratorClient.generateText(expectedPrompt)).thenReturn(iaResponse);

        GeneratedSummaryResponse response = summaryAIService.generateFromText(inputText);

        verify(contentGeneratorClient).generateText(argThat(prompt ->
                prompt.contains(inputText) && prompt.startsWith("Gere um resumo em português sobre o seguinte conteúdo")
        ));
        assertNotNull(response);
        assertEquals(iaResponse, response.content());
    }

    @Test
    void generateFromText_deveExtrairPrimeirLinhaComoSuggestedTitle() {
        String inputText = "Este é um texto sobre redes neurais com detalhes suficientes para o teste.";
        String iaResponse = "Redes Neurais: Uma Introdução\nAs redes neurais são modelos computacionais...";

        when(contentGeneratorClient.generateText(SummaryPromptBuilder.buildFromText(inputText)))
                .thenReturn(iaResponse);

        GeneratedSummaryResponse response = summaryAIService.generateFromText(inputText);

        assertEquals("Redes Neurais: Uma Introdução", response.suggestedTitle());
        assertEquals(iaResponse, response.content());
        assertNotNull(response.suggestedTags());
        assertTrue(response.suggestedTags().isEmpty());
    }

    @Test
    void generateFromText_deveRemoverMarcacaoMarkdownDoTitulo() {
        String inputText = "Texto sobre estruturas de dados com conteúdo suficiente para o teste.";
        String iaResponse = "## Estruturas de Dados\nArvores binárias são estruturas hierárquicas.";

        when(contentGeneratorClient.generateText(SummaryPromptBuilder.buildFromText(inputText)))
                .thenReturn(iaResponse);

        GeneratedSummaryResponse response = summaryAIService.generateFromText(inputText);

        assertEquals("Estruturas de Dados", response.suggestedTitle());
    }

    @Test
    void generateFromText_quandoClientLancaExcecao_devePropagar() {
        String inputText = "Texto válido com tamanho suficiente para passar na validação do serviço.";

        when(contentGeneratorClient.generateText(SummaryPromptBuilder.buildFromText(inputText)))
                .thenThrow(new ContentGeneratorException("Serviço indisponível"));

        assertThrows(ContentGeneratorException.class,
                () -> summaryAIService.generateFromText(inputText));
    }

    @Test
    void generateFromText_comTextoVazio_deveLancarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> summaryAIService.generateFromText(""));
    }

    @Test
    void generateFromText_comTextoMuitoCurto_deveLancarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> summaryAIService.generateFromText("abc"));
    }

    @Test
    void generateFromText_comTextoMuitoLongo_deveLancarIllegalArgumentException() {
        String textoGigante = "a".repeat(50001);
        assertThrows(IllegalArgumentException.class,
                () -> summaryAIService.generateFromText(textoGigante));
    }

    // -------------------------------------------------------------------------
    // generateFromTopic
    // -------------------------------------------------------------------------

    @Test
    void generateFromTopic_deveEnviarPromptCorretoAoClient() {
        String topic = "Paradigmas de Programação";
        String iaResponse = "Paradigmas de Programação\nOs principais paradigmas são...";
        String expectedPrompt = SummaryPromptBuilder.buildFromTopic(topic);

        when(contentGeneratorClient.generateText(expectedPrompt)).thenReturn(iaResponse);

        GeneratedSummaryResponse response = summaryAIService.generateFromTopic(topic);

        verify(contentGeneratorClient).generateText(argThat(prompt ->
                prompt.contains(topic) && prompt.startsWith("Gere um resumo em português, didático e objetivo")
        ));
        assertNotNull(response);
        assertEquals(iaResponse, response.content());
    }

    @Test
    void generateFromTopic_deveExtrairPrimeirLinhaComoSuggestedTitle() {
        String topic = "Banco de Dados Relacionais";
        String iaResponse = "Banco de Dados Relacionais\nSão sistemas que organizam dados em tabelas.";

        when(contentGeneratorClient.generateText(SummaryPromptBuilder.buildFromTopic(topic)))
                .thenReturn(iaResponse);

        GeneratedSummaryResponse response = summaryAIService.generateFromTopic(topic);

        assertEquals("Banco de Dados Relacionais", response.suggestedTitle());
        assertTrue(response.suggestedTags().isEmpty());
    }

    @Test
    void generateFromTopic_comTopicVazio_deveLancarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> summaryAIService.generateFromTopic(""));
    }

    @Test
    void generateFromTopic_comTopicMuitoLongo_deveLancarIllegalArgumentException() {
        String topicGigante = "t".repeat(301);
        assertThrows(IllegalArgumentException.class,
                () -> summaryAIService.generateFromTopic(topicGigante));
    }

    @Test
    void generateFromTopic_quandoRespostaIaTemApenasTitulo_suggestedTitleDeveSerPreenchido() {
        String topic = "Java";
        String iaResponse = "Java é uma linguagem de programação.";

        when(contentGeneratorClient.generateText(SummaryPromptBuilder.buildFromTopic(topic)))
                .thenReturn(iaResponse);

        GeneratedSummaryResponse response = summaryAIService.generateFromTopic(topic);

        assertNotNull(response.suggestedTitle());
        assertEquals("Java é uma linguagem de programação.", response.suggestedTitle());
    }
}
