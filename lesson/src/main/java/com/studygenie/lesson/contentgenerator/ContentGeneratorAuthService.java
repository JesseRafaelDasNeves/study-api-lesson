package com.studygenie.lesson.contentgenerator;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ContentGeneratorAuthService {

    private static final Logger log = LoggerFactory.getLogger(ContentGeneratorAuthService.class);

    @Autowired
    private ContentGeneratorProperties properties;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();

    /**
     * Retorna o token em cache, realizando login se ainda não houver um válido.
     * Nunca loga o token nem as credenciais.
     */
    public String getValidToken() {
        String token = cachedToken.get();
        if (token != null) {
            return token;
        }
        return fetchAndCacheToken();
    }

    /**
     * Invalida o token em cache. Deve ser chamado pelo cliente quando receber 401.
     */
    public void invalidateToken() {
        log.info("Token do Content Generator invalidado. Será renovado na próxima chamada.");
        cachedToken.set(null);
    }

    private String fetchAndCacheToken() {
        log.info("Realizando autenticação no Content Generator...");

        ContentGeneratorAuthRequest authRequest = new ContentGeneratorAuthRequest(
                properties.getAccessKey(),
                properties.getSecret(),
                properties.getTenantName()
        );

        try {
            ContentGeneratorAuthResponse response = RestClient.create()
                    .post()
                    .uri(properties.getAuthUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(authRequest)
                    .retrieve()
                    .body(ContentGeneratorAuthResponse.class);

            if (response == null || response.jsonToken() == null || response.jsonToken().isBlank()) {
                throw new ContentGeneratorException(
                        "Falha na autenticação com o Content Generator: resposta inválida do servidor.");
            }

            ContentGeneratorAuthTokenPayload payload =
                    new ObjectMapper().readValue(response.jsonToken(), ContentGeneratorAuthTokenPayload.class);

            if (payload.accessToken() == null || payload.accessToken().isBlank()) {
                throw new ContentGeneratorException(
                        "Falha na autenticação com o Content Generator: token ausente na resposta.");
            }

            cachedToken.set(payload.accessToken());
            log.info("Autenticação no Content Generator realizada com sucesso.");
            return payload.accessToken();

        } catch (ContentGeneratorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao autenticar no Content Generator: {}", e.getMessage());
            throw new ContentGeneratorException(
                    "Não foi possível autenticar no serviço de geração de conteúdo. Tente novamente mais tarde.");
        }
    }
}
