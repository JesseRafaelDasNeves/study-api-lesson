package com.studygenie.lesson.contentgenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// DTOs internos — usados apenas pelo cliente HTTP, não expostos ao frontend.

record ContentGeneratorAuthRequest(
        String accessKey,
        String secret,
        String tenantName
) {}

/** Envelope externo retornado pelo servidor: { "jsonToken": "<json-string>" } */
record ContentGeneratorAuthResponse(
        String jsonToken
) {}

/** Payload interno do jsonToken após deserialização: contém o access_token. */
@JsonIgnoreProperties(ignoreUnknown = true)
record ContentGeneratorAuthTokenPayload(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("token_type") String tokenType
) {}

record ContentGeneratorCompletionsRequest(
        String prompt,
        String provider,
        ContentGeneratorCompletionsParameters parameters
) {}

record ContentGeneratorCompletionsParameters(
        @JsonProperty("max_tokens") int maxTokens,
        double temperature,
        String model
) {}

record ContentGeneratorCompletionsResponse(
        String text,
        int promptTokens,
        int replyTokens
) {}
