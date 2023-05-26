package com.weigandt.bot.handlers.commands;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.CommandDto;
import com.weigandt.bot.ContextDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.bot.handlers.AbstractGptChatHandler;
import com.weigandt.history.ChatHistoryLogService;

import java.io.IOException;


public abstract class AbstractGptChatCommandHandler extends AbstractGptChatHandler {

    protected AbstractGptChatCommandHandler(AnswerService answerService,
                                            SlackSupportService slackSupportService,
                                            ChatHistoryLogService chatHistoryLogService) {
        super(answerService, slackSupportService, chatHistoryLogService);
    }

    public Response makeChatGptGreatAgain(SlashCommandContext ctx, CommandDto dto) throws SlackApiException, IOException {
        ContextDto contextDto = buildCommandContext(ctx);
        makeChatGptGreatAgain(contextDto, dto);
        return ctx.ack();
    }
}
