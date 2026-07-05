package com.studygenie.lesson.ai;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.studygenie.lesson.contentgenerator.ContentGeneratorClient;

@Service
public class SummaryAIService {

    private static final int TEXT_MIN_LENGTH = 10;
    private static final int TEXT_MAX_LENGTH = 50000;
    private static final int TOPIC_MIN_LENGTH = 2;
    private static final int TOPIC_MAX_LENGTH = 300;

    @Autowired
    private ContentGeneratorClient contentGeneratorClient;

    /**
     * Gera um resumo em pt-BR a partir de um texto já extraído de um arquivo.
     * Não persiste nada — apenas retorna o conteúdo gerado para exibição no frontend.
     *
     * @param text conteúdo textual extraído (máx. 50.000 caracteres)
     * @return resposta com conteúdo gerado, título sugerido e tags (lista vazia)
     */
    public GeneratedSummaryResponse generateFromText(String text) {
        validateText(text);
        String prompt = SummaryPromptBuilder.buildFromText(text);
        String generated = contentGeneratorClient.generateText(prompt);
        return mapToResponse(generated);
    }

    /**
     * Gera um resumo em pt-BR a partir de um tópico/tema informado pelo usuário.
     * Não persiste nada — apenas retorna o conteúdo gerado para exibição no frontend.
     *
     * @param topic tópico ou tema (máx. 300 caracteres)
     * @return resposta com conteúdo gerado, título sugerido e tags (lista vazia)
     */
    public GeneratedSummaryResponse generateFromTopic(String topic) {
        validateTopic(topic);
        String prompt = SummaryPromptBuilder.buildFromTopic(topic);
        String generated = contentGeneratorClient.generateText(prompt);
        return mapToResponse(generated);
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("O texto não pode ser vazio.");
        }
        if (text.length() < TEXT_MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "O texto deve ter pelo menos " + TEXT_MIN_LENGTH + " caracteres.");
        }
        if (text.length() > TEXT_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O texto não pode exceder " + TEXT_MAX_LENGTH + " caracteres.");
        }
    }

    private void validateTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("O tópico não pode ser vazio.");
        }
        if (topic.length() < TOPIC_MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "O tópico deve ter pelo menos " + TOPIC_MIN_LENGTH + " caracteres.");
        }
        if (topic.length() > TOPIC_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O tópico não pode exceder " + TOPIC_MAX_LENGTH + " caracteres.");
        }
    }

    /**
     * Mapeia o texto gerado pela IA para o DTO de resposta.
     * suggestedTitle: primeira linha não vazia do texto gerado.
     * suggestedTags: lista vazia (extração sofisticada é melhoria futura).
     */
    private GeneratedSummaryResponse mapToResponse(String generatedText) {
        String suggestedTitle = extractFirstLine(generatedText);
        return new GeneratedSummaryResponse(suggestedTitle, generatedText, List.of());
    }

    private String extractFirstLine(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isBlank()) {
                // Remove marcações markdown de título (ex: "# Título" → "Título")
                return trimmed.replaceAll("^#+\\s*", "");
            }
        }
        return null;
    }
}
