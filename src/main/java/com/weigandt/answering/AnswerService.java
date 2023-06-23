package com.weigandt.answering;

import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.bot.dto.ContextDto;
import com.weigandt.bot.dto.QuestionDto;
import io.reactivex.Flowable;

import java.io.IOException;
import java.util.List;

public interface AnswerService {

    Flowable<ChatCompletionChunk> getAnswerAsync(String question, List<Message> history, String botUserId);
    void answer(ContextDto contextDto, QuestionDto dto) throws SlackApiException, IOException;
}
