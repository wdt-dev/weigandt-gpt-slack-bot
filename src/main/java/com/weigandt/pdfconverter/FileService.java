package com.weigandt.pdfconverter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static com.weigandt.Constants.FILE_PROCESSOR.PDF_EXT;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.stream.Collectors.toSet;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class FileService {
    public Set<File> getPdfFilesFromSource(String sourceDir) {
        return Stream.ofNullable(new File(sourceDir).listFiles())
                .flatMap(Arrays::stream)
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().endsWith(PDF_EXT))
                .collect(toSet());
    }

    public File createTmpFile(MultipartFile file) throws IOException {
        var attributes = PosixFilePermissions.asFileAttribute(Set.of(OWNER_READ, OWNER_WRITE));
        File uploaded = Files.createTempFile("uploaded", ".pdf", attributes).toFile();
        file.transferTo(uploaded);
        return uploaded;
    }
}
