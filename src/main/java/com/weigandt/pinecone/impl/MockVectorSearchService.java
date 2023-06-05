package com.weigandt.pinecone.impl;

import com.weigandt.pinecone.VectorSearchService;
import io.pinecone.proto.QueryVector;
import io.pinecone.proto.SingleQueryResults;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.weigandt.Constants.OPENAI.FEATURE_NOT_ENABLED_MSG;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
@Profile("!use-embeddings")
public class MockVectorSearchService implements VectorSearchService {
    @Override
    public QueryVector queryVectorFromValues(List<Float> values, String namespace) {
        return getMockAnswer();
    }

    @Override
    public SingleQueryResults search(List<Float> values) {
        return getMockAnswer();
    }

    @Override
    public SingleQueryResults search(List<Float> values, String namespace) {
        return getMockAnswer();
    }

    @Override
    public SingleQueryResults search(List<Float> values, String namespace, int topK) {
        return getMockAnswer();
    }

    @Override
    public List<String> getTextBlocks(SingleQueryResults result) {
        return getMockAnswer();
    }

    private static <T> T getMockAnswer() {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return null;
    }
}
