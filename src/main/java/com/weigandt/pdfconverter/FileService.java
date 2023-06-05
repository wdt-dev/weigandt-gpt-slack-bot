package com.weigandt.pdfconverter;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface FileService {
    Set<File> getPdfFilesFromSource(String sourceDir);

    File createTmpFile(MultipartFile file) throws IOException;
}
