package com.weigandt.bot.handlers.events;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageChangedEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.dto.QuestionDto;
import com.weigandt.bot.services.SlackSupportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.trim;

@Service
@Slf4j
public class MessageChangedEventHandler extends AbstractGptChatEventHandler implements BoltEventHandler<MessageChangedEvent> {
    public MessageChangedEventHandler(AnswerService answerService,
                                      SlackSupportService slackSupportService) {
        super(answerService, slackSupportService);
    }

    @Override
    public Response apply(EventsApiPayload<MessageChangedEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        MessageChangedEvent event = payload.getEvent();
        log.debug("MessageChangedEventHandler content:{}", event);
        String newText = trim(event.getMessage().getText());
        String oldText = trim(event.getPreviousMessage().getMessage().getText());
        if (StringUtils.equals(newText, oldText)) {
            log.debug("Message content equals, no actions needed");
            return ctx.ack();
        }

        String prefix = "Your msg was changed, new answer:";
        QuestionDto dto = new QuestionDto(event, prefix);
        return makeChatGptGreatAgain(ctx, dto);
    }
}
