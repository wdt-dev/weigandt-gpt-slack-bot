package com.weigandt.bot;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Data
@Builder
public class EventDto {
    private String question;
    private final String inputText;
    private final String user;
    private final String threadTs;

    public EventDto(String inputText, String user, String threadTs, String ts) {
        this.inputText = inputText;
        this.user = user;
        this.threadTs = Optional.ofNullable(threadTs).filter(StringUtils::isNotBlank).orElse(ts);
    }
}
