package com.studygenie.lesson.contentgenerator;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ContentGeneratorClient {

    private static final Logger log = LoggerFactory.getLogger(ContentGeneratorClient.class);

    private static final String PROVIDER = "AZURE_OPEN_AI";
    private static final int MAX_TOKENS = 2000;
    private static final double TEMPERATURE = 0.5;
    private static final String MODEL = "gpt-3.5-turbo";

    @Autowired
    private ContentGeneratorProperties properties;

    @Autowired
    private ContentGeneratorAuthService authService;

    /**
     * Envia o prompt ao Content Generator e retorna o texto gerado pela IA.
     * Em caso de 401, invalida o cache, refaz login e tenta uma única vez.
     * Nunca faz retry em timeout (para não duplicar custo de chamadas de IA).
     *
     * @param prompt texto do prompt a ser enviado
     * @return texto gerado pela IA
     * @throws ContentGeneratorException em caso de falha de comunicação ou erro persistente
     */
    public String generateText(String prompt) {
        String token = authService.getValidToken();
        try {
            return callCompletions(prompt, token);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info("Content Generator retornou 401. Renovando token e repetindo a chamada...");
                authService.invalidateToken();
                String newToken = authService.getValidToken();
                try {
                    return callCompletions(prompt, newToken);
                } catch (RestClientResponseException retryEx) {
                    log.error("Content Generator retornou erro após renovação de token: status={}",
                            retryEx.getStatusCode().value());
                    throw new ContentGeneratorException(
                            "O serviço de geração de conteúdo não está disponível no momento. Tente novamente mais tarde.");
                } catch (Exception retryEx) {
                    log.error("Erro inesperado ao chamar Content Generator após renovação de token: {}",
                            retryEx.getMessage());
                    throw new ContentGeneratorException(
                            "Erro inesperado ao comunicar com o serviço de geração de conteúdo.");
                }
            }
            log.error("Content Generator retornou erro HTTP: status={}", e.getStatusCode().value());
            throw new ContentGeneratorException(
                    "O serviço de geração de conteúdo retornou um erro. Tente novamente mais tarde.");
        } catch (Exception e) {
            log.error("Falha de comunicação com o Content Generator: {}", e.getMessage());
            throw new ContentGeneratorException(
                    "Não foi possível conectar ao serviço de geração de conteúdo. Verifique a conexão e tente novamente.");
        }
    }

    private String callCompletions(String prompt, String token) {
        Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());

        ContentGeneratorCompletionsRequest request = new ContentGeneratorCompletionsRequest(
                prompt,
                PROVIDER,
                new ContentGeneratorCompletionsParameters(MAX_TOKENS, TEMPERATURE, MODEL)
        );

        ContentGeneratorCompletionsResponse response = RestClient.builder()
                .requestInterceptor((req, body, execution) -> {
                    var result = execution.execute(req, body);
                    return result;
                })
                .build()
                .post()
                .uri(properties.getCompletionsUrl())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED,
                        (req, res) -> {
                            throw new RestClientResponseException(
                                    "Unauthorized",
                                    HttpStatus.UNAUTHORIZED.value(),
                                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                    res.getHeaders(),
                                    null,
                                    null
                            );
                        })
                .body(ContentGeneratorCompletionsResponse.class);

        if (response == null || response.text() == null || response.text().isBlank()) {
            throw new ContentGeneratorException(
                    "O serviço de geração de conteúdo retornou uma resposta vazia.");
        }

        return response.text();
    }
}
