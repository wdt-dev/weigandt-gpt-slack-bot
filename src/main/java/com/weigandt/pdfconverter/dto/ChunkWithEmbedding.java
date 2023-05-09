package com.weigandt.pdfconverter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChunkWithEmbedding {

    private String chunk;
    private List<Float> embedding;
}
