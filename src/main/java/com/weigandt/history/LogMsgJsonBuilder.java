package com.weigandt.history;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogMsgJsonBuilder {
    private String datetime;
    private String userName;
    private String channelId;
    private String channelName;
    private String channelType;
    private String question;
    private String answer;
    private int questionSymbolsCount;
    private int answerSymbolsCount;
}
