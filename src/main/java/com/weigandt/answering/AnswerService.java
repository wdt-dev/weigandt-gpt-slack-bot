package com.weigandt.answering;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import io.reactivex.Flowable;

import java.util.List;

public interface AnswerService {

    public Flowable<ChatCompletionChunk> getExtrasAnswerWithStreaming(String question,
                                                                      List<Message> chatHistory,
                                                                      String botUserId);

    String getDefaultGptAnswer(String question);

    Flowable<ChatCompletionChunk> getAnswerWithStreaming(String question);

    long getSoftThresholdMs();

    long getHardThresholdMs();
}
