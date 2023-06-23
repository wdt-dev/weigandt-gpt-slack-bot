package com.weigandt.bot.handlers.events;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.weigandt.answering.AnswerService;
import com.weigandt.bot.dto.ContextDto;
import com.weigandt.bot.dto.QuestionDto;
import com.weigandt.bot.services.SlackSupportService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class AbstractGptChatEventHandler {
    private final AnswerService answerService;
    private final SlackSupportService slackSupportService;

    public Response makeChatGptGreatAgain(EventContext ctx, QuestionDto dto)
            throws SlackApiException, IOException {
        ContextDto contextDto = getSlackSupportService().buildContext(ctx);
        if (slackSupportService.isValidQuestion(contextDto, dto)) {
            answerService.answer(contextDto, dto);
        }
        return ctx.ack();
    }
}
