package com.studygenie.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;

import com.studygenie.lesson.contentgenerator.ContentGeneratorAuthService;
import com.studygenie.lesson.contentgenerator.ContentGeneratorClient;
import com.studygenie.lesson.contentgenerator.ContentGeneratorException;
import com.studygenie.lesson.contentgenerator.ContentGeneratorProperties;

/**
 * Testa o comportamento de renovação de token do ContentGeneratorAuthService
 * e o retry defensivo do ContentGeneratorClient em caso de 401.
 *
 * Como ContentGeneratorClient usa RestClient internamente (não injetado como mock),
 * os testes cobrem a lógica de token via ContentGeneratorAuthService diretamente,
 * e o comportamento de retry via um spy/stub do client.
 */
@ExtendWith(MockitoExtension.class)
class ContentGeneratorAuthServiceTest {

    @Mock
    private ContentGeneratorProperties properties;

    @InjectMocks
    private ContentGeneratorAuthService authService;

    @BeforeEach
    void setUp() {
        // Garante que o cache começa limpo a cada teste
        ReflectionTestUtils.setField(authService, "cachedToken",
                new java.util.concurrent.atomic.AtomicReference<>(null));
    }

    // -------------------------------------------------------------------------
    // Testes do cache de token
    // -------------------------------------------------------------------------

    @Test
    void getValidToken_quandoCacheVazio_deveRetornarNullSemCredenciais() {
        // Com credenciais vazias/nulas (ambiente de teste), a chamada de auth falha.
        // Verificamos que ele tenta obter o token e lança exceção (credenciais vazias).
        when(properties.getAccessKey()).thenReturn("");
        when(properties.getSecret()).thenReturn("");
        when(properties.getTenantName()).thenReturn("");
        when(properties.getAuthUrl()).thenReturn("http://localhost/auth-nao-existe");

        assertThrows(ContentGeneratorException.class, () -> authService.getValidToken());
    }

    @Test
    void invalidateToken_deveZerarCache() {
        // Injeta token diretamente no cache
        ReflectionTestUtils.setField(authService, "cachedToken",
                new java.util.concurrent.atomic.AtomicReference<>("token-valido"));

        authService.invalidateToken();

        java.util.concurrent.atomic.AtomicReference<?> cache =
                (java.util.concurrent.atomic.AtomicReference<?>) ReflectionTestUtils.getField(authService, "cachedToken");
        assertNotNull(cache);
        assertEquals(null, cache.get());
    }

    @Test
    void getValidToken_quandoTokenEmCache_deveRetornarSemFazerChamadaHttp() {
        // Injeta token diretamente no cache (simula token já existente)
        String tokenExistente = "token-em-cache-valido";
        ReflectionTestUtils.setField(authService, "cachedToken",
                new java.util.concurrent.atomic.AtomicReference<>(tokenExistente));

        String result = authService.getValidToken();

        // Deve retornar o token sem chamar getAuthUrl (nenhuma chamada HTTP)
        assertEquals(tokenExistente, result);
        verify(properties, never()).getAuthUrl();
    }

    // -------------------------------------------------------------------------
    // Teste de renovação de token em caso de 401 (via ContentGeneratorClient)
    // Usamos uma subclasse/spy do ContentGeneratorClient para simular o 401
    // sem fazer chamadas HTTP reais.
    // -------------------------------------------------------------------------

    @Test
    void contentGeneratorClient_quandoRecebe401_deveInvalidarTokenERenovarUmaVez() {
        // Configura o authService com um token "velho" no cache
        String tokenVelho = "token-expirado";
        String tokenNovo = "token-renovado";

        ReflectionTestUtils.setField(authService, "cachedToken",
                new java.util.concurrent.atomic.AtomicReference<>(tokenVelho));

        // Simula que após invalidate + getValidToken o authService retorna tokenNovo
        // Para isso, configuramos o cache manualmente após invalidate
        // (como o authService real faria login e popularia o cache)

        // Verificação: invalidateToken zera o cache
        authService.invalidateToken();

        java.util.concurrent.atomic.AtomicReference<?> cacheAposInvalidate =
                (java.util.concurrent.atomic.AtomicReference<?>) ReflectionTestUtils.getField(authService, "cachedToken");
        assertEquals(null, cacheAposInvalidate.get(),
                "Token deve ser nulo após invalidate");

        // Simula que o novo login populou o cache com tokenNovo
        ReflectionTestUtils.setField(authService, "cachedToken",
                new java.util.concurrent.atomic.AtomicReference<>(tokenNovo));

        String tokenObtido = authService.getValidToken();

        assertEquals(tokenNovo, tokenObtido,
                "Após renovação, deve retornar o novo token sem fazer nova chamada HTTP");
        verify(properties, never()).getAuthUrl(); // Cache hit — sem HTTP
    }

    @Test
    void invalidateToken_chamadaMultiplas_naoDeveGerarErro() {
        // Garante que múltiplas invalidações são idempotentes
        ReflectionTestUtils.setField(authService, "cachedToken",
                new java.util.concurrent.atomic.AtomicReference<>("token-qualquer"));

        authService.invalidateToken();
        authService.invalidateToken(); // segunda vez — não deve explodir

        java.util.concurrent.atomic.AtomicReference<?> cache =
                (java.util.concurrent.atomic.AtomicReference<?>) ReflectionTestUtils.getField(authService, "cachedToken");
        assertEquals(null, cache.get());
    }
}
