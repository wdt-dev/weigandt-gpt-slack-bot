package com.weigandt.bot;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.AppMentionEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.history.ChatHistoryLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("Message in AppMentionEvent: {}", event.getText());

//        if (!slackSupportService.isValidToAnswerMsg(event, ctx.getBotUserId())) {
//            ctx.logger.info("Bot not replying to self messages");
//            return ctx.ack();
//        }

        String question = slackSupportService.cleanupMessage(event.getText());

        String answer = answerService.getDefaultGptAnswer(question);
        String user = event.getUser();
        String respText = String.format("<@%s> %s", user, answer);

        ChatPostMessageResponse messageResponse = ctx.client().chatPostMessage(r -> r
                .channel(event.getChannel())
                .threadTs(event.getThreadTs())
                .replyBroadcast(true)
                .token(ctx.getBotToken())
                .blocks(slackSupportService.wrapInBlock(respText)));
        if (!messageResponse.isOk()) {
            ctx.logger.error("chat.postMessage failed: {}", messageResponse.getError());
            return ctx.ack();
        }
        chatHistoryLogService.logQAForUser(user, question, answer);
        return ctx.ack();
    }
}
