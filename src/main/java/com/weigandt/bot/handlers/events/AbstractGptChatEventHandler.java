package com.weigandt.bot.handlers.events;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.dto.ContextDto;
import com.weigandt.bot.dto.QuestionDto;
import com.weigandt.bot.handlers.AbstractGptChatHandler;
import com.weigandt.bot.services.SlackSupportService;
import com.weigandt.chatsettings.service.TokenUsageService;
import com.weigandt.history.ChatHistoryLogService;

import java.io.IOException;

public abstract class AbstractGptChatEventHandler extends AbstractGptChatHandler {
    protected AbstractGptChatEventHandler(AnswerService answerService,
                                          SlackSupportService slackSupportService,
                                          ChatHistoryLogService chatHistoryLogService,
                                          TokenUsageService tokenUsageService) {
        super(answerService, slackSupportService, chatHistoryLogService, tokenUsageService);
    }

    public Response makeChatGptGreatAgain(EventContext ctx, QuestionDto dto)
            throws SlackApiException, IOException {
        ContextDto contextDto = buildContext(ctx);
        super.makeChatGptGreatAgain(contextDto, dto);
        return ctx.ack();
    }
}
