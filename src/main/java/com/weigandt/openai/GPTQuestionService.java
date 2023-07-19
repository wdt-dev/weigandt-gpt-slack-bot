package com.weigandt.openai;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import io.reactivex.Flowable;

import java.util.List;

public interface GPTQuestionService {
    Flowable<ChatCompletionChunk> askAsync(String question, List<Message> chatHistory,
                                           String botUserId);
}
