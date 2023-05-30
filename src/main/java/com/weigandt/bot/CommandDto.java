package com.weigandt.bot;

import lombok.Getter;
import lombok.Setter;

@Getter
public class CommandDto {
    @Setter
    protected String question;
    protected final String inputText;
    protected final String user;
    protected final String answerPrefix;

    public CommandDto(String inputText, String user, String answerPrefix) {
        this.inputText = inputText;
        this.user = user;
        this.answerPrefix = answerPrefix;
    }
}
