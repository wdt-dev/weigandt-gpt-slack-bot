package com.weigandt.pinecone;

import com.weigandt.pdfconverter.dto.ChunkWithEmbedding;
import com.weigandt.pdfconverter.dto.VectorData;

import java.util.List;

public interface VectorService {

    List<VectorData> toVectorsData(List<ChunkWithEmbedding> chunkWithEmbeddings);
    List<VectorData> toVectorsData(String fileName, List<ChunkWithEmbedding> chunkWithEmbeddings);

}
