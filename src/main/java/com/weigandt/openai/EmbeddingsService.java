package com.weigandt.openai;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import com.weigandt.pdfconverter.dto.ChunkWithEmbedding;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.weigandt.Constants.OPENAI.VARIOUS_TEXT;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class EmbeddingsService {

    @Value("${openai.embeddings.model}")
    private String embeddingsModel;
    private final OpenAiService openAiService;

    public List<Float> createEmbeddingsForPinecone(String input) {
        List<Embedding> embeddings = createEmbeddings(input);
        return embeddingToFloats(embeddings);
    }

    public List<ChunkWithEmbedding> createEmbeddingsForFile(String filename, List<String> chunks) {
        log.debug("Creating embeddings start for file: {}", filename);
        List<ChunkWithEmbedding> chunkWithEmbeddings = chunks.stream()
                .map(this::addEmbedding)
                .toList();
        log.debug("Creating embeddings finished for file: {}", filename);
        return chunkWithEmbeddings;
    }

    public List<ChunkWithEmbedding> createEmbeddingsForText(List<String> chunks) {
        return createEmbeddingsForFile(VARIOUS_TEXT, chunks);
    }

    private List<Embedding> createEmbeddings(String input) {
        log.debug("Create embedding for text: {}", StringUtils.substring(input, 0, 50));
        return openAiService.createEmbeddings(embeddingRequest(input)).getData();
    }

    private EmbeddingRequest embeddingRequest(String input) {
        return EmbeddingRequest.builder()
                .input(Collections.singletonList(input))
                .model(getEmbeddingsModel())
                .build();
    }

    private List<Float> embeddingToFloats(List<Embedding> embeddings) {
        return embeddings.get(0).getEmbedding().stream().map(Double::floatValue).toList();
    }

    private ChunkWithEmbedding addEmbedding(String chunk) {
        return new ChunkWithEmbedding(chunk, embeddingToFloats(createEmbeddings(chunk)));
    }

}
