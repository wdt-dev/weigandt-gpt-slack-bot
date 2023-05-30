package com.weigandt.bot.handlers.commands;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class HelloCommandHandler implements SlashCommandHandler {
    @Override
    public Response apply(SlashCommandRequest request, SlashCommandContext ctx) {
        Logger logger = ctx.logger;
        logger.debug("ExtrasCommandHandler content:{}", request);

        return ctx.ack("Welcome to Weigandt GPT bot!");
    }
}
