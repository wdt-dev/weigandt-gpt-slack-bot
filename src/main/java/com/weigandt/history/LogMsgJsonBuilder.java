package com.weigandt.history;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogMsgJsonBuilder {
    private String datetime;
    private String userName;
    private String channelName;
    private String question;
    private String answer;
    private int questionSymbolsCount;
    private int answerSymbolsCount;
}
