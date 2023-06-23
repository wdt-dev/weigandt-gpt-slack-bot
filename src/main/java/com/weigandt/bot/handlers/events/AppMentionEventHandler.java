package com.weigandt.bot.handlers.events;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.AppMentionEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.dto.QuestionDto;
import com.weigandt.bot.services.SlackSupportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Slf4j
public class AppMentionEventHandler extends AbstractGptChatEventHandler implements BoltEventHandler<AppMentionEvent> {
    public AppMentionEventHandler(AnswerService answerService,
                                  SlackSupportService slackSupportService) {
        super(answerService, slackSupportService);
    }

    @Override
    public Response apply(EventsApiPayload<AppMentionEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        AppMentionEvent event = payload.getEvent();
        log.debug("AppMentionEventHandler content:{}", event);
        QuestionDto dto = new QuestionDto(event, EMPTY);
        return makeChatGptGreatAgain(ctx, dto);
    }
}
