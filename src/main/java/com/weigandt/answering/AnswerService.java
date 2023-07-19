package com.weigandt.answering;

import com.slack.api.methods.SlackApiException;
import com.weigandt.bot.dto.ContextDto;
import com.weigandt.bot.dto.QuestionDto;

import java.io.IOException;

public interface AnswerService {
    void answer(ContextDto contextDto, QuestionDto dto) throws SlackApiException, IOException;
}
