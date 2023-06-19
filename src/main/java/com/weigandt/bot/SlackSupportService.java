package com.weigandt.bot;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.request.conversations.ConversationsInfoRequest;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.weigandt.chatsettings.service.TokenUsageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.weigandt.Constants.SEARCH.BOT_IS_TYPING;
import static com.weigandt.Constants.SEARCH.HAS_JOINED_MSG;
import static com.weigandt.Constants.SEARCH.LIMITS_MSG;
import static com.weigandt.Constants.SEARCH.THE_ANSWER_IS_HUGE_MSG;
import static com.weigandt.Constants.SLACK_BOT.CHAT_POST_MESSAGE_FAILED;
import static com.weigandt.Constants.SLACK_BOT.TOKEN_LIMIT_EXCEEDED;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Getter
@Slf4j
@RequiredArgsConstructor
public class SlackSupportService {

    private static final int ONLY_LAST_THREAD_MSG = 1;
    private final Map<String, String> userIdToUserName = new ConcurrentHashMap<>();
    private final Map<String, Conversation> channelsCache = new ConcurrentHashMap<>();
    private final List<String> trashSymbols = Arrays.asList("@", "<", ">");
    private final List<String> trashMsgs = Arrays.asList(LIMITS_MSG, BOT_IS_TYPING, THE_ANSWER_IS_HUGE_MSG, HAS_JOINED_MSG);
    @Value("${chat.history.limit:10}")
    private Integer historyLimit;

    private final TokenUsageService tokenUsageService;

    public List<Message> getMsgHistory(QuestionDto dto, ContextDto ctxDto) throws SlackApiException, IOException {
        var historyRequest = ConversationsHistoryRequest.builder()
                .channel(ctxDto.channelId())
                .limit(ONLY_LAST_THREAD_MSG)
                .token(ctxDto.botToken())
                .inclusive(true)
                .includeAllMetadata(true)
                .build();
        var history = ctxDto.client().conversationsHistory(historyRequest);
        List<Message> messages = history.getMessages();
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }
        Optional<Message> threadHeadMsgOpt = messages.stream()
                .filter(m -> StringUtils.equals(m.getThreadTs(), m.getTs()))
                .findFirst();
        if (threadHeadMsgOpt.isEmpty()) {
            log.info("No chat history to use");
            return Collections.emptyList();
        }
        //thread start msg
        Message threadHeadMsg = threadHeadMsgOpt.get();
        ConversationsRepliesRequest request = ConversationsRepliesRequest.builder()
                .token(ctxDto.botToken())
                .limit(historyLimit)
                .channel(ctxDto.channelId())
                .ts(threadHeadMsg.getTs())
                .build();
        ConversationsRepliesResponse replies = ctxDto.client().conversationsReplies(request);
        List<Message> filteredMessages = Stream.concat(Stream.of(threadHeadMsg),
                        emptyIfNull(replies.getMessages()).stream())
                .filter(this::isNotTechMsg)
                .map(m -> {
                    m.setText(cleanupMessage(m.getText()));
                    return m;
                })
                .toList();
        log.info("Filtered history with replies: {}", filteredMessages.stream().map(Message::getText).toList());
        return filteredMessages;
    }

    private boolean isNotTechMsg(Message x) {
        return trashMsgs.stream().noneMatch(tm -> StringUtils.contains(x.getText(), tm));
    }

    public boolean isCorrectToAnswerMsg(String text, String botUserId, boolean isIm) {
        if (isIm) {
            return isNotBlank(text);
        }
        return StringUtils.contains(text, String.format("<@%s>", botUserId));
    }

    public String cleanupMessage(String input) {
        if (trashSymbols.stream().anyMatch(input::contains)) {
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

    public boolean isHardThresholdExceeded(ContextDto contextDto, QuestionDto dto, Logger logger, Conversation channelInfo) throws SlackApiException, IOException {
        String botToken = contextDto.botToken();
        MethodsClient client = contextDto.client();
        String userName = this.getCachedUserFullName(dto.getUser(),
                client, botToken);
        if (tokenUsageService.isHardThresholdExceeded(userName)) {
            Integer tokenUsedTotal = tokenUsageService.getTodayStatistics(userName).get().getTokenUsedTotal();
            String respText = String.format(TOKEN_LIMIT_EXCEEDED, tokenUsedTotal);
            ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                    .channel(channelInfo.getId())
                    .threadTs(dto.getThreadTs())
                    .token(botToken)
                    .blocks(this.wrapInBlock(respText)));
            if (!messageResponse.isOk()) {
                logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
            }
            return true;
        }
        return false;
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

    public Conversation getCachedChannelInfo(ContextDto contextDto) throws SlackApiException, IOException {
        String channel = contextDto.channelId();
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        if (!channelsCache.containsKey(channel)) {
            Conversation channelInfo = getChannelInfo(client, channel, botToken);
            if (isNull(channelInfo)) {
                contextDto.logger().debug("Channel info is empty");
                return null;
            }
            channelsCache.put(channel, channelInfo);
        }
        return channelsCache.get(channel);
    }
}
