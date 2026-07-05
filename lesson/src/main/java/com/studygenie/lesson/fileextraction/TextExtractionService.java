package com.studygenie.lesson.fileextraction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Serviço responsável por orquestrar a extração de texto de arquivos recebidos
 * via upload. Todo o processamento ocorre em memória — nenhum arquivo é salvo
 * em disco ou em qualquer storage.
 *
 * <p>O extrator correto é selecionado dinamicamente a partir dos {@link TextExtractor}
 * registrados como @Component no contexto do Spring. Para suportar um novo formato,
 * basta registrar uma nova implementação de TextExtractor — sem alterar este serviço.
 */
@Service
public class TextExtractionService {

    /** Mapeamento de extensão de arquivo → content-type canônico, como fallback. */
    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
            "pdf",  "application/pdf",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "txt",  "text/plain"
    );

    @Autowired
    private List<TextExtractor> extractors;

    @Autowired
    private FileExtractionProperties properties;

    /**
     * Extrai o texto bruto do arquivo recebido via upload.
     *
     * <p>Validações realizadas antes da extração:
     * <ol>
     *   <li>Arquivo não pode estar vazio</li>
     *   <li>Tamanho não pode exceder {@code file-extraction.max-size-mb}</li>
     *   <li>Content-type deve ser suportado (com fallback pela extensão do nome)</li>
     * </ol>
     *
     * @param file arquivo recebido via multipart/form-data
     * @return DTO com texto extraído e metadados do arquivo
     * @throws IllegalArgumentException   se o arquivo estiver vazio ou exceder o limite
     * @throws UnsupportedFileTypeException se nenhum extrator suportar o tipo
     * @throws TextExtractionException    se o conteúdo do arquivo for ilegível
     */
    public ExtractedTextResponse extractFrom(MultipartFile file) throws TextExtractionException {
        validateFile(file);

        String resolvedContentType = resolveContentType(file);
        TextExtractor extractor = findExtractor(resolvedContentType);

        try {
            String text = extractor.extract(file.getInputStream());
            return new ExtractedTextResponse(
                    file.getOriginalFilename(),
                    resolvedContentType,
                    text,
                    text.length()
            );
        } catch (IOException e) {
            throw new TextExtractionException(
                    "Não foi possível ler o conteúdo do arquivo enviado.", e);
        }
    }

    // -------------------------------------------------------------------------
    // Validações
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }
        if (file.getSize() > properties.maxSizeBytes()) {
            throw new IllegalArgumentException(
                    "O arquivo excede o tamanho máximo permitido de " + properties.maxSizeMb() + " MB.");
        }
    }

    // -------------------------------------------------------------------------
    // Resolução de content-type
    // -------------------------------------------------------------------------

    /**
     * Determina o content-type efetivo do arquivo.
     * Estratégia: usa o content-type declarado pelo multipart; se não suportado,
     * tenta inferir pela extensão do nome do arquivo como camada extra de checagem.
     */
    private String resolveContentType(MultipartFile file) {
        String declared = file.getContentType();

        // 1. Verifica se algum extrator suporta o content-type declarado
        if (declared != null && isSupported(declared)) {
            return declared;
        }

        // 2. Fallback: inferir pelo sufixo do nome do arquivo
        String filename = file.getOriginalFilename();
        if (filename != null && filename.contains(".")) {
            String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            String inferred = EXTENSION_TO_CONTENT_TYPE.get(extension);
            if (inferred != null && isSupported(inferred)) {
                return inferred;
            }
        }

        // 3. Nenhum extrator suportado — usa o tipo declarado para a mensagem de erro
        throw new UnsupportedFileTypeException(declared != null ? declared : "desconhecido");
    }

    private boolean isSupported(String contentType) {
        return extractors.stream().anyMatch(e -> e.supports(contentType));
    }

    private TextExtractor findExtractor(String contentType) {
        return extractors.stream()
                .filter(e -> e.supports(contentType))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileTypeException(contentType));
    }
}
