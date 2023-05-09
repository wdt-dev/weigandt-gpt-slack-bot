package com.weigandt.bot;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import org.springframework.stereotype.Service;

@Service
public class HelloCommandHandler implements SlashCommandHandler {
    @Override
    public Response apply(SlashCommandRequest request, SlashCommandContext ctx) {
        return ctx.ack("Welcome to Weigandt GPT bot!");
    }
}
