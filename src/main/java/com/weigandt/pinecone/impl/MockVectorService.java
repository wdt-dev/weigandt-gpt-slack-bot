package com.weigandt.pinecone.impl;

import com.weigandt.pdfconverter.dto.ChunkWithEmbedding;
import com.weigandt.pdfconverter.dto.VectorData;
import com.weigandt.pinecone.VectorService;
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
@Profile("!create-embeddings")
public class MockVectorService implements VectorService {
    @Override
    public List<VectorData> toVectorsData(List<ChunkWithEmbedding> chunkWithEmbeddings) {
        return getMockAnswer();
    }

    @Override
    public List<VectorData> toVectorsData(String fileName, List<ChunkWithEmbedding> chunkWithEmbeddings) {
        return getMockAnswer();
    }

    private static <T> List<T> getMockAnswer() {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return emptyList();
    }
}
