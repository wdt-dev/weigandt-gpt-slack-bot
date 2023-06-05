package com.weigandt.pinecone;

import io.pinecone.proto.QueryVector;
import io.pinecone.proto.SingleQueryResults;

import java.util.List;

public interface VectorSearchService {

    QueryVector queryVectorFromValues(List<Float> values, String namespace);
    SingleQueryResults search(List<Float> values);
    SingleQueryResults search(List<Float> values, String namespace);
    SingleQueryResults search(List<Float> values, String namespace, int topK);
    List<String> getTextBlocks(SingleQueryResults result);
}
