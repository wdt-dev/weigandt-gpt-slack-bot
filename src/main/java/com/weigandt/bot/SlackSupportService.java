package com.weigandt.bot;

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.model.Message;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.event.AppMentionEvent;
import com.slack.api.model.event.MessageEvent;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@Getter
public class SlackSupportService {

    public List<Message> getMsgHistory(MessageEvent event, EventContext ctx) throws SlackApiException, IOException {
        ConversationsHistoryRequest historyRequest = ConversationsHistoryRequest.builder()
                .channel(event.getChannel())
                .token(ctx.getBotToken())
                .build();
        ConversationsHistoryResponse history = ctx.client().conversationsHistory(historyRequest);
        return history.getMessages();
    }

    public List<Message> getMsgHistory(SlashCommandPayload payload, SlashCommandContext ctx) throws SlackApiException, IOException {
        ConversationsHistoryRequest historyRequest = ConversationsHistoryRequest.builder()
                .channel(payload.getChannelId())
                .token(ctx.getBotToken())
                .build();
        ConversationsHistoryResponse history = ctx.client().conversationsHistory(historyRequest);
        return history.getMessages();
    }

    public boolean isValidToAnswerMsg(MessageEvent event, String botUserId) {
        String input = event.getText();
        return input.contains(userMentionedText(botUserId));
    }

    private static String userMentionedText(String botUserId) {
        return String.format("<@%s>", botUserId);
    }

    public boolean isValidToAnswerMsg(AppMentionEvent event, String botUserId) {
        String input = event.getText();
        return input.contains(userMentionedText(botUserId));
    }

    public boolean isValidToAnswerMsg(SlashCommandPayload payload, String botUserId) {
        String input = payload.getText();
        return input.contains(userMentionedText(botUserId));
    }

    public String cleanupMessage(String input) {
        if (input.contains("@") || input.contains("<") || input.contains(">")) {
            return StringUtils.trim(input.replaceAll("<@.*?>", ""));
        }
        return input;
    }

    public List<LayoutBlock> wrapInBlock(String respText) {
        return Collections.singletonList(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(respText)
                        .build())
                .build());
    }
}
