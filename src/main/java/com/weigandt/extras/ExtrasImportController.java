package com.weigandt.extras;

import com.weigandt.extras.pdf.PdfFileProcessorService;
import com.weigandt.extras.text.TextProcessorService;
import com.weigandt.openai.EmbeddingsService;
import com.weigandt.pdfconverter.FileService;
import com.weigandt.pdfconverter.impl.DefaultFileService;
import com.weigandt.pdfconverter.dto.ChunkWithEmbedding;
import com.weigandt.pdfconverter.dto.VectorData;
import com.weigandt.pinecone.VectorCreateService;
import com.weigandt.pinecone.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller("/data/import")
@Slf4j
@Profile("create-embeddings")
public class ExtrasImportController {

    @Resource
    private TextProcessorService textProcessorService;
    @Resource
    private PdfFileProcessorService pdfFileProcessorService;
    @Resource
    private EmbeddingsService embeddingsService;
    @Resource
    private VectorService vectorService;
    @Resource
    private VectorCreateService vectorCreateService;
    @Resource
    private FileService fileService;

    @PostMapping("/text")
    public ResponseEntity<String> createFromText(@RequestBody String text) {
        List<String> chunks = textProcessorService.splitToChunks(text);
        List<ChunkWithEmbedding> chunksWithEmbeddings = embeddingsService.createEmbeddingsForText(chunks);
        List<VectorData> vectorData = vectorService.toVectorsData(chunksWithEmbeddings);
        vectorData.forEach(data -> vectorCreateService.upsertVectors(data.getVectors()));
        log.debug(StringUtils.join(chunks));
        return ResponseEntity.ok("Text imported successfully");
    }

    @PostMapping("/file")
    public ResponseEntity<String> createFromFile(@RequestParam("file") MultipartFile file) {
        try {
            File tmpFile = fileService.createTmpFile(file);
            String text = pdfFileProcessorService.getContent(tmpFile);
            List<String> chunks = textProcessorService.splitToChunks(text);
            List<ChunkWithEmbedding> chunksWithEmbeddings = embeddingsService.createEmbeddingsForText(chunks);
            List<VectorData> vectorData = vectorService.toVectorsData(tmpFile.getName(),chunksWithEmbeddings);
            vectorData.forEach(data -> vectorCreateService.upsertVectors(data.getVectors()));
            return ResponseEntity.ok("File imported successfully");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Something went wrong, try again later");
        }
    }
}
