package com.weigandt.bot;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Message;
import com.slack.api.model.event.MessageEvent;
import com.weigandt.answering.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageEventHandler implements BoltEventHandler<MessageEvent> {

    private final SlackSupportService slackSupportService;
    private final AnswerService answerService;

    @Override
    public Response apply(EventsApiPayload<MessageEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        MessageEvent event = payload.getEvent();
        String input = event.getText();
        String user = event.getUser();

        if (slackSupportService.isValidToAnswerMsg(input, user)) {
            ctx.logger.info("Bot not replying to self messages");
            return ctx.ack();
        }

        List<Message> messages = slackSupportService.getMsgHistory(event, ctx);
        String answer = answerService.getAnswer(input, messages);
        String respText = String.format("<@%s> %s", user, answer);

        ChatPostMessageResponse messageResponse = ctx.client().chatPostMessage(r -> r
                .channel(event.getChannel())
                .replyBroadcast(true)
                //.token(BOT_TOKEN)
                .token(ctx.getBotToken())
                .text(respText));
        if (!messageResponse.isOk()) {
            ctx.logger.error("chat.postMessage failed: {}", messageResponse.getError());
        }
        return ctx.ack();
    }
}
