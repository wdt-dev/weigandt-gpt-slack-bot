package com.weigandt.bot.handlers;

import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.CommandDto;
import com.weigandt.bot.ContextDto;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.bot.handlers.GPTCompletionStreamProcessor;
import com.weigandt.history.ChatHistoryLogService;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractGptChatHandler {

    private final AnswerService answerService;
    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;

    public void makeChatGptGreatAgain(ContextDto contextDto, EventDto dto) throws SlackApiException, IOException {
        Logger logger = contextDto.logger();
        Conversation channelInfo = slackSupportService.getCachedChannelInfo(contextDto.channelId(), contextDto.client(), contextDto.botToken());

        if (!slackSupportService.isCorrectToAnswerMsg(dto.getInputText(), contextDto.botUserId(), channelInfo.isIm())) {
            logger.info("Bot not tagged or isn't an IM channel , ignore");
            return;
        }

        String question = slackSupportService.cleanupMessage(dto.getInputText());
        dto.setQuestion(question);
        GPTCompletionStreamProcessor processor =
                new GPTCompletionStreamProcessor(slackSupportService, chatHistoryLogService, contextDto, channelInfo, dto);
        Flowable<ChatCompletionChunk> answerStream = answerService.getAnswerWithStreaming(question);
        answerStream.doOnError(processor::processException).subscribe(processor::processChunks);
    }

    public void makeChatGptGreatAgain(ContextDto contextDto, CommandDto dto) throws SlackApiException, IOException {
        Logger logger = contextDto.logger();
        Conversation channelInfo = slackSupportService.getCachedChannelInfo(contextDto.channelId(), contextDto.client(), contextDto.botToken());

        if (!slackSupportService.isCorrectToAnswerMsg(dto.getInputText(), contextDto.botUserId(), channelInfo.isIm())) {
            logger.info("Bot not tagged or isn't an IM channel , ignore");
            return;
        }

        String question = slackSupportService.cleanupMessage(dto.getInputText());
        dto.setQuestion(question);
        List<Message> messages = slackSupportService.getMsgHistory(contextDto.channelId(), contextDto);

        GPTCompletionStreamProcessor processor =
                new GPTCompletionStreamProcessor(slackSupportService, chatHistoryLogService, contextDto, channelInfo, dto);
        Flowable<ChatCompletionChunk> answerStream = answerService.getAnswerStreaming(question, messages, contextDto.botUserId());
        answerStream.doOnError(processor::processException).subscribe(processor::processChunks);
    }
}
