package com.weigandt.bot.handlers;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.AppMentionEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.history.ChatHistoryLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        String question = slackSupportService.cleanupMessage(event.getText());

        String answer = answerService.getDefaultGptAnswer(question);
        String user = event.getUser();
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
            return ctx.ack();
        }
        String userFullName = slackSupportService.getUserFullName(user, client, ctx.getBotToken());

        chatHistoryLogService.logQAForUser(userFullName, question, answer);
        return ctx.ack();
    }
}
