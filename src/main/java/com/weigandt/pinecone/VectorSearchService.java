package com.weigandt.pinecone;

import com.google.protobuf.Struct;
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
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class VectorSearchService {

    @Value("${pinecone.namespace}")
    private String namespace;
    @Value("${pinecone.topK}")
    private String topK;
    private static final String DEFAULT_NAMESPACE = "docsNamespace";
    private static final int DEFAULT_TOP_K = 2;
    private final PineconeConnection connection;

    public QueryVector queryVectorFromValues(List<Float> values, String namespace) {
        return QueryVector.newBuilder()
                .addAllValues(values)
                .setNamespace(namespace)
                .build();
    }

    public SingleQueryResults search(List<Float> values) {
        return search(values, namespace());
    }

    public SingleQueryResults search(List<Float> values, String namespace) {
        return search(values, namespace, topK());
    }

    public SingleQueryResults search(List<Float> values, String namespace, int topK) {
        QueryRequest request = QueryRequest.newBuilder()
                .addQueries(queryVectorFromValues(values, namespace))
                .setTopK(topK)
                .build();
        return getFirstResult(connection.getBlockingStub().query(request));
    }

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

    private int topK() {
        return NumberUtils.isDigits(topK) ? Integer.parseInt(topK) : DEFAULT_TOP_K;
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
