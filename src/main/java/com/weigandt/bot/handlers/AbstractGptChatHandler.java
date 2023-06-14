package com.weigandt.bot.handlers;

import com.slack.api.RequestConfigurator;
import com.slack.api.bolt.context.Context;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.CommandDto;
import com.weigandt.bot.ContextDto;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.chatsettings.service.TokenUsageService;
import com.weigandt.history.ChatHistoryLogService;
import io.reactivex.Flowable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static com.weigandt.Constants.SLACK_BOT.CHAT_POST_MESSAGE_FAILED;
import static com.weigandt.Constants.SLACK_BOT.NO_CHAT_ACCESS;
import static com.weigandt.Constants.SLACK_BOT.TOKEN_LIMIT_EXCEEDED;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Getter
public abstract class AbstractGptChatHandler {

    private final AnswerService answerService;
    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;
    private final TokenUsageService tokenUsageService;

    public void makeChatGptGreatAgain(ContextDto contextDto, EventDto dto) throws SlackApiException, IOException {
        Logger logger = contextDto.logger();
        logger.debug("Context dto: {}", contextDto);
        Conversation channelInfo = slackSupportService.getCachedChannelInfo(contextDto);
        if (isNull(channelInfo)) {
            logger.warn(NO_CHAT_ACCESS, contextDto.channelId());
            return;
        }
        if (!slackSupportService.isCorrectToAnswerMsg(dto.getInputText(), contextDto.botUserId(), channelInfo.isIm())) {
            logger.info("Bot not tagged or isn't an IM channel, ignore");
            return;
        }
        if (isHardThresholdExceeded(contextDto, dto, logger, channelInfo)) {
            return;
        }

        String question = slackSupportService.cleanupMessage(dto.getInputText());
        dto.setQuestion(question);
        List<Message> history = slackSupportService.getMsgHistory(contextDto.channelId(), contextDto);

        GPTCompletionStreamProcessor processor =
                new GPTCompletionStreamProcessor(slackSupportService, chatHistoryLogService, tokenUsageService,
                        contextDto, channelInfo, dto);
        Flowable<ChatCompletionChunk> answerStream = answerService.getAnswerAsync(question, history, contextDto.botUserId());
        answerStream.doOnError(processor::processException).subscribe(processor::processChunks);
    }

    private boolean isHardThresholdExceeded(ContextDto contextDto, CommandDto dto, Logger logger, Conversation channelInfo) throws SlackApiException, IOException {
        String botToken = contextDto.botToken();
        MethodsClient client = contextDto.client();
        String userName = slackSupportService.getCachedUserFullName(dto.getUser(),
                client, botToken);
        if (tokenUsageService.isHardThresholdExceeded(userName)) {
            Integer tokenUsedTotal = tokenUsageService.getTodayStatistics(userName).get().getTokenUsedTotal();
            String respText = String.format(TOKEN_LIMIT_EXCEEDED, tokenUsedTotal);
            RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder> postMsgConfigurator = r -> r
                    .channel(channelInfo.getId())
                    .threadTs((dto instanceof EventDto eventDto) ? eventDto.getThreadTs() : null)
                    .token(botToken)
                    .blocks(slackSupportService.wrapInBlock(respText));
            ChatPostMessageResponse messageResponse = client.chatPostMessage(postMsgConfigurator);
            if (!messageResponse.isOk()) {
                logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
            }
            return true;
        }
        return false;
    }

    public void makeChatGptGreatAgain(ContextDto contextDto, CommandDto dto) throws SlackApiException, IOException {
        Logger logger = contextDto.logger();
        logger.debug("Context dto: {}", contextDto);
        Conversation channelInfo = slackSupportService.getCachedChannelInfo(contextDto);
        if (isNull(channelInfo)) {
            logger.warn(NO_CHAT_ACCESS, contextDto.channelId());
            return;
        }
        if (!slackSupportService.isCorrectToAnswerMsg(dto.getInputText(), contextDto.botUserId(), channelInfo.isIm())) {
            logger.info("Bot not tagged or isn't an IM channel , ignore");
            return;
        }
        if (isHardThresholdExceeded(contextDto, dto, logger, channelInfo)) {
            return;
        }

        String question = slackSupportService.cleanupMessage(dto.getInputText());
        dto.setQuestion(question);
        List<Message> messages = slackSupportService.getMsgHistory(contextDto.channelId(), contextDto);

        var processor = new GPTCompletionStreamProcessor(slackSupportService, chatHistoryLogService, tokenUsageService,
                contextDto, channelInfo, dto);
        Flowable<ChatCompletionChunk> answerStream = answerService.getExtrasAnswerAsync(question, messages, contextDto.botUserId());
        answerStream.doOnError(processor::processException).subscribe(processor::processChunks);
    }

    protected ContextDto buildContext(Context ctx) {
        String botToken = ctx.getBotToken();
        String botUserId = ctx.getBotUserId();
        String channelId = null;
        if (ctx instanceof EventContext eCtx) {
            channelId = eCtx.getChannelId();
        }
        MethodsClient client = ctx.client();
        Logger logger = ctx.getLogger();
        long softThresholdMs = getAnswerService().getSoftThresholdMs();
        long hardThresholdMs = getAnswerService().getHardThresholdMs();
        return new ContextDto(botToken, client, logger, botUserId, channelId,
                softThresholdMs, hardThresholdMs);
    }
}
