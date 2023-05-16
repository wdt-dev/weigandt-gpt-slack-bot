package com.weigandt.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.weigandt.Constants.SLACK_BOT.LOG_EXT;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatHistoryLogService {

    @Value("${chat.history.base.path}")
    private String basePath;

    public void logQAForUser(String user, String question, String answer) {
        if (!Files.exists(Paths.get(basePath))) {
            log.error("Unknown path for logs provided, pls check: {}", basePath);
            return;
        }
        Path logFilePath = Paths.get(basePath, user + LOG_EXT);
        if (!Files.exists(logFilePath)) {
            createLogFileForUser(logFilePath);
        }

        writeToLogFile("Question: " + question, logFilePath);
        writeToLogFile("Answer: " + answer, logFilePath);
    }

    private static void writeToLogFile(String text, Path logFilePath) {
        try {
            Files.writeString(logFilePath, text, StandardOpenOption.APPEND);
            Files.writeString(logFilePath, System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Can't write to log file {}: {}", logFilePath, e.getMessage(), e);
        }
    }

    private static void createLogFileForUser(Path logFilePath) {
        try {
            Files.createFile(logFilePath);
        } catch (IOException e) {
            log.error("Can't create log file {}: {}", logFilePath, e.getMessage(), e);
        }
    }
}
