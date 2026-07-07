package com.studygenie.lesson.ai;

/**
 * Monta os prompts enviados ao Content Generator para geração de resumos.
 * Os templates são constantes isoladas para facilitar ajuste sem alterar a
 * lógica dos serviços.
 */
public class SummaryPromptBuilder {

    private static final String FROM_TEXT_TEMPLATE = "Gere um resumo em português sobre o seguinte conteúdo, destacando os pontos principais:\n\n{text}. Orienteções\n\n - Retorne texto puro, apenas com formatação de parágrafos e quebra de linha.";

    private static final String FROM_TOPIC_TEMPLATE = "Gere um resumo em português, didático e objetivo, sobre o seguinte tema: {topic}\n\n - Retorne texto puro, apenas com formatação de parágrafos e quebra de linha.";

    /**
     * Constrói o prompt para geração de resumo a partir de um texto já extraído.
     *
     * @param text conteúdo textual extraído do arquivo
     * @return prompt formatado pronto para envio ao Content Generator
     */
    public static String buildFromText(String text) {
        return FROM_TEXT_TEMPLATE.replace("{text}", text);
    }

    /**
     * Constrói o prompt para geração de resumo a partir de um tópico/tema.
     *
     * @param topic tópico ou tema informado pelo usuário
     * @return prompt formatado pronto para envio ao Content Generator
     */
    public static String buildFromTopic(String topic) {
        return FROM_TOPIC_TEMPLATE.replace("{topic}", topic);
    }

    private SummaryPromptBuilder() {
        // Classe utilitária — não instanciável
    }
}
