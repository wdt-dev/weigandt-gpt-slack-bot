package com.weigandt.bot.handlers;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.history.ChatHistoryLogService;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class AbstractGptChatEventHandler {

    private final AnswerService answerService;
    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;

    public Response makeChatGptGreatAgain(EventContext ctx,EventDto dto) throws SlackApiException, IOException {
        Logger logger = ctx.logger;
        MethodsClient client = ctx.client();
        String botToken = ctx.getBotToken();
        String channelId = ctx.getChannelId();
        Conversation channelInfo = slackSupportService.getCachedChannelInfo(channelId, client, botToken);

        if (!slackSupportService.isCorrectToAnswerMsg(dto.getInputText(), ctx, channelInfo.isIm())) {
            logger.info("Bot not tagged or isn't an IM channel , ignore");
            return ctx.ack();
        }

        String question = slackSupportService.cleanupMessage(dto.getInputText());
        dto.setQuestion(question);
        GPTCompletionStreamProcessor processor = new GPTCompletionStreamProcessor(slackSupportService, chatHistoryLogService, ctx, channelInfo, dto);
        Flowable<ChatCompletionChunk> answerStream = answerService.getAnswerWithStreaming(question);
        answerStream.subscribe(processor::processChunks);

        return ctx.ack();
    }
}
