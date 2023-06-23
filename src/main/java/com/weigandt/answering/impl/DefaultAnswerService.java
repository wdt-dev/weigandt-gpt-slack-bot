package com.weigandt.answering.impl;

import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.dto.ContextDto;
import com.weigandt.bot.dto.QuestionDto;
import com.weigandt.bot.handlers.GPTCompletionStreamProcessor;
import com.weigandt.bot.services.SlackSupportService;
import com.weigandt.chatsettings.service.TokenUsageService;
import com.weigandt.history.ChatHistoryLogService;
import com.weigandt.openai.GPTQuestionService;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAnswerService implements AnswerService {

    private final GPTQuestionService gptQuestionService;
    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;
    private final TokenUsageService tokenUsageService;

    @Override
    public Flowable<ChatCompletionChunk> getAnswerAsync(String question, List<Message> history, String botUserId) {
        return gptQuestionService.askAsync(question, history, botUserId);
    }
    @Override
    public void answer(ContextDto contextDto, QuestionDto dto) throws SlackApiException, IOException {
        String botUserId = contextDto.botUserId();
        String question = slackSupportService.cleanupMessage(dto.getRawInputText());
        dto.setQuestion(question);

        List<Message> history = slackSupportService.getMsgHistory(dto, contextDto);

        Conversation channelInfo = slackSupportService.getCachedChannelInfo(contextDto);

        GPTCompletionStreamProcessor processor =
                new GPTCompletionStreamProcessor(slackSupportService, chatHistoryLogService, tokenUsageService,
                        contextDto, channelInfo, dto);
        Flowable<ChatCompletionChunk> answerStream = gptQuestionService.askAsync(question, history, botUserId);
        answerStream.doOnError(processor::processException).subscribe(processor::processChunks);
    }
}
