package com.weigandt.bot.handlers;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.history.ChatHistoryLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MessageEventHandler implements BoltEventHandler<MessageEvent> {

    private final SlackSupportService slackSupportService;
    private final AnswerService answerService;
    private final ChatHistoryLogService chatHistoryLogService;

    @Override
    public Response apply(EventsApiPayload<MessageEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        MessageEvent event = payload.getEvent();
        Logger logger = ctx.logger;
        if (StringUtils.containsNone(event.getText(), String.format("<@%s>", ctx.getBotUserId()))) {
            logger.info("Bot not tagged, ignore");
            return ctx.ack();
        }

        String user = event.getUser();
        String question = slackSupportService.cleanupMessage(event.getText());

        String answer = answerService.getDefaultGptAnswer(question);
        String respText = String.format("<@%s> %s", user, answer);

        MethodsClient client = ctx.client();
        ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                .channel(event.getChannel())

                .threadTs(event.getThreadTs())
                .replyBroadcast(true)
                .token(ctx.getBotToken())
                .blocks(slackSupportService.wrapInBlock(respText)));
        if (!messageResponse.isOk()) {
            logger.error("chat.postMessage failed: {}", messageResponse.getError());
        }
        String userFullName = slackSupportService.getUserFullName(user, client, ctx.getBotToken());
        chatHistoryLogService.logQAForUser(userFullName, question, answer);
        return ctx.ack();
    }
}
