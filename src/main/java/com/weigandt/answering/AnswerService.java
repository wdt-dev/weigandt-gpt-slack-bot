package com.weigandt.answering;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import io.reactivex.Flowable;

import java.util.List;

public interface AnswerService {

    Flowable<ChatCompletionChunk> getAnswerAsync(String question, List<Message> history, String botUserId);

    long getSoftThresholdMs();

    long getHardThresholdMs();
}
