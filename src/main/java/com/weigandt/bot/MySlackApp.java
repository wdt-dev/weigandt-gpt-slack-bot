package com.weigandt.bot;

import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;
import com.slack.api.model.event.AppMentionEvent;
import com.slack.api.model.event.MessageEvent;
import org.springframework.stereotype.Service;

import static com.weigandt.Constants.SLACK_BOT.CMD_EXTRAS;
import static com.weigandt.Constants.SLACK_BOT.CMD_HELLO;

@Service
public class MySlackApp extends App {

    public MySlackApp(SlashCommandHandler helloCommandHandler,
                      SlashCommandHandler extrasCommandHandler,
                      BoltEventHandler<MessageEvent> messageEventHandler,
                      BoltEventHandler<AppMentionEvent> appMentionEventHandler) {
        this.command(CMD_HELLO, helloCommandHandler);
        this.command(CMD_EXTRAS, extrasCommandHandler);
        this.event(AppMentionEvent.class, appMentionEventHandler);
        this.event(MessageEvent.class, messageEventHandler);
    }
}
