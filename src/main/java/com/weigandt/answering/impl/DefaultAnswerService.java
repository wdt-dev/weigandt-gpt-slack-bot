package com.weigandt.answering.impl;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.answering.AnswerService;
import com.weigandt.openai.GPTQuestionService;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAnswerService implements AnswerService {

    private final GPTQuestionService gptQuestionService;

    @Override
    public Flowable<ChatCompletionChunk> getAnswerAsync(String question, List<Message> history, String botUserId) {
        return gptQuestionService.askAsync(question, history, botUserId);
    }

    @Override
    public long getSoftThresholdMs() {
        return gptQuestionService.getSoftThresholdMs();
    }

    @Override
    public long getHardThresholdMs() {
        return gptQuestionService.getHardThresholdMs();
    }
}
