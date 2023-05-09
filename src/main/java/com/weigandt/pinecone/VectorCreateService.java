package com.weigandt.pinecone;

import io.pinecone.PineconeConnection;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.weigandt.Constants.OPENAI.VARIOUS_TEXT;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class VectorCreateService {
    private final PineconeConnection pineconeConnection;

    public UpsertResponse upsertVectors(List<Vector> vectors) {
        return upsertVectors(VARIOUS_TEXT, vectors);
    }

    public UpsertResponse upsertVectors(String namespace, List<Vector> vectors) {
        UpsertRequest upsertRequest = UpsertRequest.newBuilder()
                .addAllVectors(vectors)
                .setNamespace(namespace)
                .build();

        return pineconeConnection.getBlockingStub().upsert(upsertRequest);
    }
}
