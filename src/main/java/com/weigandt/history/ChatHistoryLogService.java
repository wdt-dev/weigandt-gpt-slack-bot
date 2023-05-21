package com.weigandt.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.weigandt.Constants.SLACK_BOT.LOG_EXT;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatHistoryLogService {

    private static final Type LIST_OF_LOGS_TYPE = new TypeToken<List<LogMsgJsonBuilder>>() {
    }.getType();
    @Value("${chat.history.base.path}")
    private String basePath;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public void logQAForUser(String user, String question, String answer) throws IOException {
        Path base = Paths.get(basePath);
        if (!Files.exists(base)) {
            log.error("Unknown path for logs provided, try to create.. ");
            createDirectory(base);
        }
        Path logFilePath = Paths.get(basePath, user + LOG_EXT);
        if (!Files.exists(logFilePath)) {
            createLogFileForUser(logFilePath);
        }

        writeToLogFile("Question: " + question, logFilePath);
        writeToLogFile("Answer: " + answer, logFilePath);
    }

    public void logCommunicationForUser(String user, LogMsgJsonBuilder logEntry) throws IOException {
        Path base = Paths.get(basePath);
        if (!Files.exists(base)) {
            log.error("Unknown path for logs provided, try to create.. ");
            createDirectory(base);
        }
        Path logFilePath = Paths.get(basePath, user + LOG_EXT);
        if (!Files.exists(logFilePath)) {
            createLogFileForUser(logFilePath);
            gson.toJson(singletonList(logEntry), new FileWriter(logFilePath.toFile()));
            return;
        }
        List<LogMsgJsonBuilder> listOfLogs;
        try (FileReader fr = new FileReader(logFilePath.toFile())) {
            listOfLogs = gson.fromJson(fr, LIST_OF_LOGS_TYPE);
        }
        if (isNull(listOfLogs)) {
            listOfLogs = new ArrayList<>();
        }
        listOfLogs.add(logEntry);
        try (FileWriter fw = new FileWriter(logFilePath.toFile())) {
            gson.toJson(listOfLogs, fw);
        }
    }

    private void createDirectory(Path base) throws IOException {
        try {
            Files.createDirectory(base);
        } catch (IOException e) {
            log.error("Can't create base path {}: {}", basePath, e.getMessage(), e);
            throw e;
        }
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
