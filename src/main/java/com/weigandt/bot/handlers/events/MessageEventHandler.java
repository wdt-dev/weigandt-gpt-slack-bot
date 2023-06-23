package com.weigandt.bot.handlers.events;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.dto.QuestionDto;
import com.weigandt.bot.services.SlackSupportService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
public class MessageEventHandler extends AbstractGptChatEventHandler implements BoltEventHandler<MessageEvent> {
    public MessageEventHandler(AnswerService answerService,
                               SlackSupportService slackSupportService) {
        super(answerService, slackSupportService);
    }

    @Override
    public Response apply(EventsApiPayload<MessageEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        MessageEvent event = payload.getEvent();
        Logger logger = ctx.logger;
        logger.debug("MessageEventHandler content:{}", event);

        QuestionDto dto = new QuestionDto(event, EMPTY);
        return makeChatGptGreatAgain(ctx, dto);
    }
}
