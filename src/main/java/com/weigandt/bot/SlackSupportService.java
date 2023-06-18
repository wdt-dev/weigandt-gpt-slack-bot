package com.weigandt.bot;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.request.conversations.ConversationsInfoRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.weigandt.Constants.SEARCH.BOT_IS_TYPING;
import static com.weigandt.Constants.SEARCH.LIMITS_MSG;
import static com.weigandt.Constants.SEARCH.THE_ANSWER_IS_HUGE_MSG;
import static com.weigandt.Constants.SLACK_BOT.CHAT_POST_MESSAGE_FAILED;
import static com.weigandt.Constants.SLACK_BOT.TOKEN_LIMIT_EXCEEDED;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Service
@Getter
@Slf4j
@RequiredArgsConstructor
public class SlackSupportService {

    private final Map<String, String> userIdToUserName = new ConcurrentHashMap<>();
    private final Map<String, Conversation> channelsCache = new ConcurrentHashMap<>();
    private final List<String> trashSymbols = Arrays.asList("@", "<", ">");

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    @Value("${chat.extras.prefix:with extras}")
    private String extrasPrefix;
    @Value("${chat.history.limit:10}")
    private Integer historyLimit;

    private final TokenUsageService tokenUsageService;

    public List<Message> getMsgHistory(QuestionDto dto, ContextDto ctxDto) throws SlackApiException, IOException {
        var historyRequest = ConversationsHistoryRequest.builder()
                .channel(ctxDto.channelId())
                .limit(historyLimit)
                .token(ctxDto.botToken())
                .build();
        var history = ctxDto.client().conversationsHistory(historyRequest);
        log.info("Original history: {}", history.getMessages());
        List<Message> historyResult = emptyIfNull(history.getMessages()).stream()
                .filter(msg -> getOnlyMsgsFromThread(msg, dto.getThreadTs()))
                .filter(this::ignoreTechMsgs)
                .toList();
        log.info("Filtered history: {}", historyResult.stream().map(Message::getText).toList());
        return historyResult;
    }

    private boolean getOnlyMsgsFromThread(Message x, String threadTs) {
        return StringUtils.equals(threadTs, x.getThreadTs());
    }

    private boolean ignoreTechMsgs(Message x) {
        return isFalse(StringUtils.contains(x.getText(), LIMITS_MSG)
                || StringUtils.contains(x.getText(), BOT_IS_TYPING)
                || StringUtils.contains(x.getText(), THE_ANSWER_IS_HUGE_MSG));
    }

    public boolean isCorrectToAnswerMsg(String text, String botUserId, boolean isIm) {
        if (isIm) {
            return StringUtils.isNotBlank(text);
        }
        return StringUtils.contains(text, String.format("<@%s>", botUserId));
    }

    public String cleanupMessage(String input) {
        if (trashSymbols.stream().anyMatch(input::contains)) {
            return StringUtils.trim(input.replaceAll("<@.*?>", ""));
        }
        return input;
    }

    public String cleanupExtrasMessage(String input) {
        String result = input;
        if (input.contains(extrasPrefix)) {
            result = result.replace(extrasPrefix, "");
        }
        return cleanupMessage(result);
    }

    public List<LayoutBlock> wrapInBlock(String respText) {
        return Collections.singletonList(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(respText)
                        .build())
                .build());
    }

    public boolean isExtrasRequest(String question) {
        return StringUtils.contains(question, extrasPrefix);
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
