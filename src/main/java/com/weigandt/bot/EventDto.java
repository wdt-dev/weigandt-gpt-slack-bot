package com.weigandt.bot;

import com.slack.api.model.event.AppMentionEvent;
import com.slack.api.model.event.MessageChangedEvent;
import com.slack.api.model.event.MessageEvent;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class EventDto extends CommandDto {
    @Getter
    private final String threadTs;

    public EventDto(String inputText, String user, String threadTs, String ts, String answerPrefix) {
        super(inputText, user, answerPrefix);
        this.threadTs = Optional.ofNullable(threadTs).filter(StringUtils::isNotBlank).orElse(ts);
    }

    public EventDto(AppMentionEvent event, String answerPrefix) {
        this(event.getText(), event.getUser(), event.getThreadTs(), event.getTs(), answerPrefix);
    }

    public EventDto(MessageEvent event, String answerPrefix) {
        this(event.getText(), event.getUser(), event.getThreadTs(), event.getTs(), answerPrefix);
    }

    public EventDto(MessageChangedEvent event, String answerPrefix) {
        this(event.getMessage().getText(), event.getMessage().getUser(),
                event.getMessage().getThreadTs(), event.getTs(), answerPrefix);
    }
}
