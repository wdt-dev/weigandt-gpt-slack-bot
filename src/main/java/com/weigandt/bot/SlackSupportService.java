package com.weigandt.bot;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.request.conversations.ConversationsInfoRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Conversation;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
public class SlackSupportService {

    private final Map<String, String> userIdToUserName = new ConcurrentHashMap<>();
    private final Map<String, Conversation> channelsCache = new ConcurrentHashMap<>();

    public List<Message> getMsgHistory(String channel, ContextDto ctxDto) throws SlackApiException, IOException {
        ConversationsHistoryRequest historyRequest = ConversationsHistoryRequest.builder()
                .channel(channel)
                .token(ctxDto.botToken())
                .build();
        ConversationsHistoryResponse history = ctxDto.client().conversationsHistory(historyRequest);
        return history.getMessages();
    }

    public boolean isCorrectToAnswerMsg(String text, String botUserId, boolean isIm) {
        if (isIm) {
            return StringUtils.isNotBlank(text);
        }
        return StringUtils.contains(text, String.format("<@%s>",botUserId));
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

    private String getUserFullName(String userId, MethodsClient client, String botToken) throws SlackApiException, IOException {
        UsersInfoRequest request = UsersInfoRequest.builder()
                .user(userId)
                .token(botToken)
                .build();
        UsersInfoResponse response = client.usersInfo(request);
        return response.getUser().getName();
    }

    private Conversation getChannelInfo(MethodsClient client, String channel, String botToken) throws SlackApiException, IOException {
        ConversationsInfoRequest request = ConversationsInfoRequest.builder()
                .channel(channel)
                .token(botToken)
                .build();
        return client.conversationsInfo(request).getChannel();
    }

    public String getCachedUserFullName(String user, MethodsClient client, String botToken) throws SlackApiException, IOException {
        if (!userIdToUserName.containsKey(user)) {
            String userFullName = getUserFullName(user, client, botToken);
            userIdToUserName.put(user, userFullName);
        }
        return userIdToUserName.get(user);
    }

    public Conversation getCachedChannelInfo(String channel, MethodsClient client, String botToken) throws SlackApiException, IOException {
        if (!channelsCache.containsKey(channel)) {
            Conversation channelInfo = getChannelInfo(client, channel, botToken);
            channelsCache.put(channel, channelInfo);
        }
        return channelsCache.get(channel);
    }
}
