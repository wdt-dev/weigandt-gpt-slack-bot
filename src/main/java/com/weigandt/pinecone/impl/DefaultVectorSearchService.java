package com.weigandt.pinecone.impl;

import com.google.protobuf.Struct;
import com.weigandt.pinecone.VectorSearchService;
import io.pinecone.PineconeConnection;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.QueryVector;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.SingleQueryResults;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.weigandt.Constants.OPENAI.VARIOUS_TEXT;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
@Profile("use-embeddings")
public class DefaultVectorSearchService implements VectorSearchService {

    private static final String DEFAULT_NAMESPACE = VARIOUS_TEXT;
    @Value("${pinecone.namespace:}")
    private String namespace;
    @Value("${pinecone.topK:2}")
    private Integer topK;
    private final PineconeConnection connection;

    @Override
    public QueryVector queryVectorFromValues(List<Float> values, String namespace) {
        return QueryVector.newBuilder()
                .addAllValues(values)
                .setNamespace(namespace)
                .build();
    }

    @Override
    public SingleQueryResults search(List<Float> values) {
        return search(values, namespace());
    }

    @Override
    public SingleQueryResults search(List<Float> values, String namespace) {
        return search(values, namespace, topK);
    }

    @Override
    public SingleQueryResults search(List<Float> values, String namespace, int topK) {
        QueryRequest request = QueryRequest.newBuilder()
                .addQueries(queryVectorFromValues(values, namespace))
                .setTopK(topK)
                .build();
        try {
            QueryResponse searchResponse = connection.getBlockingStub().query(request);
            return getFirstResult(searchResponse);
        } catch (Exception ex) {
            log.warn("Pinecone connection issues while searching", ex);
        }
        return null;
    }

    @Override
    public List<String> getTextBlocks(SingleQueryResults result) {
        List<String> textBlocks = result.getMatchesList().stream()
                .map(ScoredVector::getMetadata)
                .map(this::transformMetadata)
                .toList();
        log.debug("Extracting metadata from search results: {}", textBlocks);

        return textBlocks;
    }

    private String namespace() {
        return StringUtils.isNotBlank(namespace) ? namespace : DEFAULT_NAMESPACE;
    }

    private static SingleQueryResults getFirstResult(QueryResponse qResponse) {
        return Optional.ofNullable(qResponse).map(qr -> qr.getResults(0)).orElse(null);
    }

    private String transformMetadata(Struct metadata) {
        return metadata.getFieldsMap()
                .entrySet()
                .stream()
                .map(e -> e.getKey() + ": " + e.getValue().getStringValue())
                .collect(Collectors.joining(", "));
    }

}
