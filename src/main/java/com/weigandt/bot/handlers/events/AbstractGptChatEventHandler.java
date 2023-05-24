package com.weigandt.bot.handlers.events;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.ContextDto;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.bot.handlers.AbstractGptChatHandler;
import com.weigandt.history.ChatHistoryLogService;
import org.slf4j.Logger;

import java.io.IOException;

public abstract class AbstractGptChatEventHandler extends AbstractGptChatHandler {
    protected AbstractGptChatEventHandler(AnswerService answerService,
                                          SlackSupportService slackSupportService,
                                          ChatHistoryLogService chatHistoryLogService) {
        super(answerService, slackSupportService, chatHistoryLogService);
    }

    public Response makeChatGptGreatAgain(EventContext ctx, EventDto dto) throws SlackApiException, IOException {
        Logger logger = ctx.logger;
        MethodsClient client = ctx.client();
        String botToken = ctx.getBotToken();
        ContextDto contextDto = new ContextDto(botToken, client, logger, ctx.getBotUserId(), ctx.getChannelId());
        super.makeChatGptGreatAgain(contextDto, dto);
        return ctx.ack();
    }
}
