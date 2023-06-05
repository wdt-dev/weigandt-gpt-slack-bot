package com.weigandt.extras.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("create-embeddings")
public class PdfFileProcessorService {

    public String getContent(File file) {
        try {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String result = stripper.getText(document);
            document.close();
            return result;
        } catch (IOException ex) {
            return EMPTY;
        }
    }
}
