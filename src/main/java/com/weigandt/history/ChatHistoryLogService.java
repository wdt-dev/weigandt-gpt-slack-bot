package com.weigandt.history;

import java.io.IOException;

public interface ChatHistoryLogService {

    void logCommunicationForUser(String user, LogMsgJsonBuilder logEntry) throws IOException;
}
