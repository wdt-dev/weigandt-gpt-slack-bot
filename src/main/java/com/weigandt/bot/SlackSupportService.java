package com.weigandt.bot;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Message;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@Getter
public class SlackSupportService {

    public List<Message> getMsgHistory(String channel, SlashCommandContext ctx) throws SlackApiException, IOException {
        ConversationsHistoryRequest historyRequest = ConversationsHistoryRequest.builder()
                .channel(channel)
                .token(ctx.getBotToken())
                .build();
        ConversationsHistoryResponse history = ctx.client().conversationsHistory(historyRequest);
        return history.getMessages();
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

    public String getUserFullName(String userId, MethodsClient client, String botToken) throws SlackApiException, IOException {
        UsersInfoRequest request = UsersInfoRequest.builder()
                .user(userId)
                .token(botToken)
                .build();
        UsersInfoResponse response = client.usersInfo(request);
        return response.getUser().getName();
    }
}
