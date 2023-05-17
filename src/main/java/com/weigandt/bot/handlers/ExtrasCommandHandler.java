package com.weigandt.bot.handlers;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Message;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.SlackSupportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtrasCommandHandler implements SlashCommandHandler {
    private final SlackSupportService slackSupportService;
    private final AnswerService answerService;

    @Override
    public Response apply(SlashCommandRequest payload, SlashCommandContext ctx) throws SlackApiException, IOException {
        SlashCommandPayload cmdPayload = payload.getPayload();
        Logger logger = ctx.logger;

        String user = cmdPayload.getUserName();
        String question = slackSupportService.cleanupMessage(cmdPayload.getText());

        List<Message> messages = slackSupportService.getMsgHistory(cmdPayload.getChannelId(), ctx);
        String answer = answerService.getAnswer(question, messages, ctx.getBotUserId());
        String respText = String.format("<@%s> %s", user, answer);

        ChatPostMessageResponse messageResponse = ctx.client().chatPostMessage(r -> r
                .channel(cmdPayload.getChannelId())
                .replyBroadcast(true)
                .token(ctx.getBotToken())
                .blocks(slackSupportService.wrapInBlock(respText)));
        if (!messageResponse.isOk()) {
            logger.error("chat.postMessage failed: {}", messageResponse.getError());
        }
        return ctx.ack();
    }
}
