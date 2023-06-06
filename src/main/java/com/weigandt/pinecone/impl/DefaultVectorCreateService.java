package com.weigandt.pinecone.impl;

import com.weigandt.pinecone.VectorCreateService;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
@Profile("create-embeddings")
public class DefaultVectorCreateService implements VectorCreateService {
    private final PineconeConnection pineconeConnection;

    @Value("${pinecone.namespace:}")
    private final String namespace;

    @Override
    public UpsertResponse upsertVectors(List<Vector> vectors) {
        return upsertVectors(namespace, vectors);
    }

    @Override
    public UpsertResponse upsertVectors(String namespace, List<Vector> vectors) {
        UpsertRequest upsertRequest = UpsertRequest.newBuilder()
                .addAllVectors(vectors)
                .setNamespace(namespace)
                .build();
        // TODO: test with big files (maybe use asyncStub instead of blockingStub)
        try {
            return pineconeConnection.getBlockingStub().upsert(upsertRequest);
        } catch (Exception e) {
            log.warn("Pinecone connection issues while creating embeddings", e);
        }
        return null;
    }
}
