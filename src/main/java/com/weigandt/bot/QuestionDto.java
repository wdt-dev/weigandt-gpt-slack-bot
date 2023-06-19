package com.weigandt.bot;

import com.slack.api.model.event.AppMentionEvent;
import com.slack.api.model.event.MessageChangedEvent;
import com.slack.api.model.event.MessageEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Getter
public class QuestionDto {
    @Setter
    private String question;
    private final String threadTs;
    private final String rawInputText;
    private final String user;
    private final String answerPrefix;

    private QuestionDto(String rawInputText, String user, String answerPrefix, String threadTs) {
        this.rawInputText = rawInputText;
        this.user = user;
        this.answerPrefix = answerPrefix;
        this.threadTs = threadTs;
    }

    public QuestionDto(String rawInputText, String user, String threadTs, String ts, String answerPrefix) {
        this(rawInputText, user, answerPrefix, Optional.ofNullable(threadTs).filter(StringUtils::isNotBlank).orElse(ts));
    }

    public QuestionDto(AppMentionEvent event, String answerPrefix) {
        this(event.getText(), event.getUser(), event.getThreadTs(), event.getTs(), answerPrefix);
    }

    public QuestionDto(MessageEvent event, String answerPrefix) {
        this(event.getText(), event.getUser(), event.getThreadTs(), event.getTs(), answerPrefix);
    }

    public QuestionDto(MessageChangedEvent event, String answerPrefix) {
        this(event.getMessage().getText(), event.getMessage().getUser(),
                event.getMessage().getThreadTs(), event.getTs(), answerPrefix);
    }
}
