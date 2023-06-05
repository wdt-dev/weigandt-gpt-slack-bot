package com.weigandt.bot.handlers.commands;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.CommandDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.chatsettings.service.TokenUsageService;
import com.weigandt.history.ChatHistoryLogService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
public class ExtrasCommandHandler extends AbstractGptChatCommandHandler implements SlashCommandHandler {
    public ExtrasCommandHandler(AnswerService answerService,
                                SlackSupportService slackSupportService,
                                ChatHistoryLogService chatHistoryLogService,
                                TokenUsageService tokenUsageService) {
        super(answerService, slackSupportService, chatHistoryLogService, tokenUsageService);
    }

    @Override
    public Response apply(SlashCommandRequest payload, SlashCommandContext ctx) throws SlackApiException, IOException {
        SlashCommandPayload cmdPayload = payload.getPayload();
        Logger logger = ctx.logger;
        logger.debug("ExtrasCommandHandler content:{}", cmdPayload);

        CommandDto dto = new CommandDto(cmdPayload.getText(), cmdPayload.getUserName(), EMPTY);
        return makeChatGptGreatAgain(ctx, dto);
    }
}
