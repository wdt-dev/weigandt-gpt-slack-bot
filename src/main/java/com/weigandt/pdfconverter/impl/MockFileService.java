package com.weigandt.pdfconverter.impl;

import com.weigandt.pdfconverter.FileService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

import static com.weigandt.Constants.OPENAI.FEATURE_NOT_ENABLED_MSG;
import static java.util.Collections.emptySet;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
@Profile("!create-embeddings")
public class MockFileService implements FileService {
    @Override
    public Set<File> getPdfFilesFromSource(String sourceDir) {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return emptySet();
    }

    @Override
    public File createTmpFile(MultipartFile file) {
        log.warn(FEATURE_NOT_ENABLED_MSG);
        return null;
    }
}
