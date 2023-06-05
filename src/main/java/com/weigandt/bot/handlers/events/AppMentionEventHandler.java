package com.weigandt.bot.handlers.events;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.AppMentionEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.chatsettings.service.TokenUsageService;
import com.weigandt.history.ChatHistoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Slf4j
public class AppMentionEventHandler extends AbstractGptChatEventHandler implements BoltEventHandler<AppMentionEvent> {
    public AppMentionEventHandler(AnswerService answerService,
                                  SlackSupportService slackSupportService,
                                  ChatHistoryLogService chatHistoryLogService,
                                  TokenUsageService tokenUsageService) {
        super(answerService, slackSupportService, chatHistoryLogService, tokenUsageService);
    }

    @Override
    public Response apply(EventsApiPayload<AppMentionEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        AppMentionEvent event = payload.getEvent();
        Logger logger = ctx.logger;
        logger.debug("AppMentionEventHandler content:{}", event);

        EventDto dto = new EventDto(event, EMPTY);
        return makeChatGptGreatAgain(ctx, dto);
    }
}
