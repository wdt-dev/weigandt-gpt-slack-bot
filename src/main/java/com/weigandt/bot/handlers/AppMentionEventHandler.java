package com.weigandt.bot.handlers;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.event.AppMentionEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.history.ChatHistoryLogService;
import com.weigandt.history.LogMsgJsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppMentionEventHandler implements BoltEventHandler<AppMentionEvent> {

    private final AnswerService answerService;
    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;

    @Override
    public Response apply(EventsApiPayload<AppMentionEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        AppMentionEvent event = payload.getEvent();
        Logger logger = ctx.logger;
        if (!slackSupportService.isCorrectToAnswerMsg(event, ctx)) {
            logger.info("Bot not tagged or isn't an IM channel , ignore");
            return ctx.ack();
        }

        String question = slackSupportService.cleanupMessage(event.getText());

        String answer = answerService.getDefaultGptAnswer(question);
        String user = event.getUser();
        String respText = String.format("<@%s> %s", user, answer);

        MethodsClient client = ctx.client();
        String botToken = ctx.getBotToken();
        Conversation channelInfo = slackSupportService.getCachedChannelInfo(ctx.getChannelId(), client, botToken);
        if (channelInfo.isIm()) {
            logger.info("This is IM dialog");
        }
        ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                .channel(event.getChannel())
                .threadTs(event.getThreadTs())
                //.replyBroadcast(true)
                .token(botToken)
                .blocks(slackSupportService.wrapInBlock(respText)));
        if (!messageResponse.isOk()) {
            logger.error("chat.postMessage failed: {}", messageResponse.getError());
            return ctx.ack();
        }
        String userFullName = slackSupportService.getCachedUserFullName(user, client, botToken);
        LogMsgJsonBuilder logEntry = LogMsgJsonBuilder.builder()
                .datetime(String.valueOf(System.currentTimeMillis()))
                .userName(userFullName)
                .channelName(channelInfo.getId())
                .question(question)
                .answer(answer)
                .questionSymbolsCount(StringUtils.length(question))
                .answerSymbolsCount(StringUtils.length(answer))
                .build();
        chatHistoryLogService.logCommunicationForUser(userFullName, logEntry);
        return ctx.ack();
    }
}
