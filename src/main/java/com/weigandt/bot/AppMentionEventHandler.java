package com.weigandt.bot;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.AppMentionEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AppMentionEventHandler implements BoltEventHandler<AppMentionEvent> {
    @Override
    public Response apply(EventsApiPayload<AppMentionEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        AppMentionEvent event = payload.getEvent();
        ctx.say(String.format("Hello <@%s>! What can I do for you?", event.getUser()));
        return ctx.ack();
    }
}
