package com.weigandt.bot.services;

import com.slack.api.bolt.context.Context;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.block.LayoutBlock;
import com.weigandt.bot.dto.ContextDto;
import com.weigandt.bot.dto.QuestionDto;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

public interface SlackSupportService {

    List<Message> getMsgHistory(QuestionDto dto, ContextDto ctxDto) throws SlackApiException, IOException;

    boolean isCorrectToAnswerMsg(String text, String botUserId, boolean isIm);

    String cleanupMessage(String input);

    List<LayoutBlock> wrapInBlock(String respText);

    boolean isHardThresholdExceeded(ContextDto contextDto, QuestionDto dto, Conversation channelInfo)
            throws SlackApiException, IOException;

    String getCachedUserFullName(String user, MethodsClient client, String botToken)
            throws SlackApiException, IOException;

    Conversation getCachedChannelInfo(ContextDto contextDto) throws SlackApiException, IOException;

    ContextDto buildContext(Context ctx);

    boolean isValidQuestion(ContextDto contextDto, QuestionDto dto) throws SlackApiException, IOException;
}
