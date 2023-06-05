package com.weigandt.openai.impl;

import com.weigandt.openai.EmbeddingsService;
import com.weigandt.pdfconverter.dto.ChunkWithEmbedding;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.weigandt.Constants.OPENAI.FEATURE_NOT_ENABLED_MSG;
import static java.util.Collections.emptyList;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
@Profile({"!use-embeddings && !create-embeddings"})
public class MockEmbeddingsService implements EmbeddingsService {

    @Override
    public List<Float> createEmbeddingsForPinecone(String input) {
        return mockResponse();
    }

    private static <T> List<T> mockResponse() {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return emptyList();
    }

    @Override
    public List<ChunkWithEmbedding> createEmbeddingsForFile(String filename, List<String> chunks) {
        return mockResponse();
    }

    @Override
    public List<ChunkWithEmbedding> createEmbeddingsForText(List<String> chunks) {
        return mockResponse();
    }

}
