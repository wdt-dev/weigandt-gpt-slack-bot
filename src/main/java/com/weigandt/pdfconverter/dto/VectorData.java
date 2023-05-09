package com.weigandt.pdfconverter.dto;

import io.pinecone.proto.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VectorData {
    private List<Vector> vectors;
}
