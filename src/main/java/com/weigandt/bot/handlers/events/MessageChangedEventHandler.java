package com.weigandt.bot.handlers.events;

import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageChangedEvent;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.history.ChatHistoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.trim;

@Service
@Slf4j
public class MessageChangedEventHandler extends AbstractGptChatEventHandler implements BoltEventHandler<MessageChangedEvent> {
    public MessageChangedEventHandler(AnswerService answerService,
                                      SlackSupportService slackSupportService,
                                      ChatHistoryLogService chatHistoryLogService) {
        super(answerService, slackSupportService, chatHistoryLogService);
    }

    @Override
    public Response apply(EventsApiPayload<MessageChangedEvent> payload, EventContext ctx)
            throws IOException, SlackApiException {
        MessageChangedEvent event = payload.getEvent();
        Logger logger = ctx.logger;
        logger.debug("MessageChangedEventHandler content:{}", event);
        String newText = trim(event.getMessage().getText());
        String oldText = trim(event.getPreviousMessage().getMessage().getText());
        if (StringUtils.equals(newText, oldText)) {
            log.debug("Message content equals, no actions needed");
            return ctx.ack();
        }

        String user = event.getMessage().getUser();
        String threadTs = event.getMessage().getThreadTs();
        EventDto dto = new EventDto(newText, user, threadTs, event.getTs());
        return makeChatGptGreatAgain(ctx, dto);
    }
}