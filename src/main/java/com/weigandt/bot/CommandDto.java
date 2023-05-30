package com.weigandt.bot;

import lombok.Getter;
import lombok.Setter;

@Getter
public class CommandDto {
    @Setter
    protected String question;
    protected final String inputText;
    protected final String user;

    public CommandDto(String inputText, String user) {
        this.inputText = inputText;
        this.user = user;
    }
}
