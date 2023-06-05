package com.weigandt.pinecone.impl;

import com.weigandt.pinecone.VectorCreateService;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.weigandt.Constants.OPENAI.FEATURE_NOT_ENABLED_MSG;
import static com.weigandt.Constants.OPENAI.VARIOUS_TEXT;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
@Profile("!create-embeddings")
public class MockVectorCreateService implements VectorCreateService {

    @Override
    public UpsertResponse upsertVectors(List<Vector> vectors) {
        return getMockAnswer();
    }

    @Override
    public UpsertResponse upsertVectors(String namespace, List<Vector> vectors) {
        return getMockAnswer();
    }

    private static <T> T getMockAnswer() {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return null;
    }
}
