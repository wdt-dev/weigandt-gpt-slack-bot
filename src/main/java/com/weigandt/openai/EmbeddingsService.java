package com.weigandt.openai;

import com.weigandt.pdfconverter.dto.ChunkWithEmbedding;

import java.util.List;

public interface EmbeddingsService {

    List<Float> createEmbeddingsForPinecone(String input);

    List<ChunkWithEmbedding> createEmbeddingsForFile(String filename, List<String> chunks);

    List<ChunkWithEmbedding> createEmbeddingsForText(List<String> chunks);
}
