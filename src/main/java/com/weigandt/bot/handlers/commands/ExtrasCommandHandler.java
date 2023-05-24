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
import com.weigandt.history.ChatHistoryLogService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExtrasCommandHandler extends AbstractGptChatCommandHandler implements SlashCommandHandler {
    public ExtrasCommandHandler(AnswerService answerService,
                                SlackSupportService slackSupportService,
                                ChatHistoryLogService chatHistoryLogService) {
        super(answerService, slackSupportService, chatHistoryLogService);
    }

    @Override
    public Response apply(SlashCommandRequest payload, SlashCommandContext ctx) throws SlackApiException, IOException {
        SlashCommandPayload cmdPayload = payload.getPayload();
        CommandDto dto = new CommandDto(cmdPayload.getText(), cmdPayload.getUserName());
        return makeChatGptGreatAgain(ctx, dto);
    }
}
