package com.weigandt.openai;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import io.reactivex.Flowable;

import java.util.List;

public interface GPTQuestionService {

    String rephraseQuestion(String question, List<Message> chatHistory, String botUserId);

    String ask(String question);

    Flowable<ChatCompletionChunk> askAsync(String question, List<Message> chatHistory,
                                           String botUserId);

    String askWithExtras(String question, List<String> extras);

    Flowable<ChatCompletionChunk> askWithExtrasAsync(String question, List<String> extras);

    long getSoftThresholdMs();

    long getHardThresholdMs();
}
