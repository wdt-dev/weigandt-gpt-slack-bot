package com.weigandt.bot;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class EventDto extends CommandDto {
    @Getter
    private final String threadTs;

    public EventDto(String inputText, String user, String threadTs, String ts) {
        super(inputText, user);
        this.threadTs = Optional.ofNullable(threadTs).filter(StringUtils::isNotBlank).orElse(ts);
    }
}
