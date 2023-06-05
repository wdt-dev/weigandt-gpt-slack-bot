package com.weigandt.pinecone.impl;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.weigandt.pdfconverter.dto.ChunkWithEmbedding;
import com.weigandt.pdfconverter.dto.VectorData;
import com.weigandt.pinecone.VectorService;
import io.pinecone.proto.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.weigandt.Constants.OPENAI.VARIOUS_TEXT;
import static com.weigandt.Constants.PINECONE.F_FILE_NAME;
import static com.weigandt.Constants.PINECONE.F_SOURCE_TEXT;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
@Profile("create-embeddings")
public class DefaultVectorService implements VectorService {

    private static final AtomicLong idCreator = new AtomicLong();

    @Override
    public List<VectorData> toVectorsData(List<ChunkWithEmbedding> chunkWithEmbeddings) {
        return toVectorsData(VARIOUS_TEXT, chunkWithEmbeddings);
    }

    @Override
    public List<VectorData> toVectorsData(String fileName, List<ChunkWithEmbedding> chunkWithEmbeddings) {
        List<Vector> vectors = chunkWithEmbeddings.stream()
                .map(entry -> createVector(fileName, entry.getEmbedding(), entry.getChunk()))
                .toList();

        log.debug("[{}] vectors created for file:[{}]", vectors.size(), fileName);
        return ListUtils.partition(vectors, 50).stream()
                .map(VectorData::new)
                .toList();
    }

    private Vector fromValues(String id, List<Float> values, String chunk, String fileName) {
        return Vector.newBuilder()
                .setId(id)
                .setMetadata(vectorMetadata(chunk, fileName))
                .addAllValues(values)
                .build();
    }

    private Struct vectorMetadata(String chunk, String filename) {
        return Struct.newBuilder()
                .putFields(F_SOURCE_TEXT, value(chunk))
                .putFields(F_FILE_NAME, value(filename))
                .build();
    }

    private static Value value(String val) {
        return Value.newBuilder().setStringValue(val).build();
    }

    private Vector createVector(String fileName, List<Float> embedding, String sourceText) {
        return fromValues(String.valueOf(idCreator.incrementAndGet()), embedding, sourceText, fileName);
    }
}
