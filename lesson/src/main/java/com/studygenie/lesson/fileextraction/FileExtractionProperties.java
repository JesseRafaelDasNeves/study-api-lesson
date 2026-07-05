package com.studygenie.lesson.fileextraction;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração para extração de texto de arquivos.
 * Configurável via application.properties com o prefixo "file-extraction".
 *
 * <p>Exemplo de uso no properties:
 * <pre>
 * file-extraction.max-size-mb=20
 * </pre>
 *
 * @param maxSizeMb tamanho máximo permitido do arquivo em MB (padrão: 20)
 */
@ConfigurationProperties(prefix = "file-extraction")
public record FileExtractionProperties(int maxSizeMb) {

    /** Valor padrão de 20 MB caso não configurado. */
    public FileExtractionProperties {
        if (maxSizeMb <= 0) {
            maxSizeMb = 20;
        }
    }

    /**
     * Converte o limite em MB para bytes para comparação com {@link org.springframework.web.multipart.MultipartFile#getSize()}.
     *
     * @return limite em bytes
     */
    public long maxSizeBytes() {
        return (long) maxSizeMb * 1024 * 1024;
    }
}
