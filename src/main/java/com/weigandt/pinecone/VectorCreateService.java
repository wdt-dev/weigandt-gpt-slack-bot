package com.weigandt.pinecone;

import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.Vector;

import java.util.List;

public interface VectorCreateService {
    UpsertResponse upsertVectors(List<Vector> vectors);
    UpsertResponse upsertVectors(String namespace, List<Vector> vectors);
}
