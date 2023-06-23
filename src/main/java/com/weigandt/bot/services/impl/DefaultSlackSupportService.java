package com.weigandt.bot.services.impl;

import com.slack.api.bolt.context.Context;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.request.conversations.ConversationsInfoRequest;
import com.slack.api.methods.request.conversations.ConversationsRepliesRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.conversations.ConversationsRepliesResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.weigandt.bot.dto.ContextDto;
import com.weigandt.bot.dto.QuestionDto;
import com.weigandt.bot.services.SlackSupportService;
import com.weigandt.chatsettings.service.TokenUsageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import static com.weigandt.Constants.SLACK_BOT.NO_CHAT_ACCESS;
import static com.weigandt.Constants.SLACK_BOT.TOKEN_LIMIT_EXCEEDED;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Getter
@Slf4j
@RequiredArgsConstructor
public class DefaultSlackSupportService implements SlackSupportService {

    private static final int ONLY_LAST_THREAD_MSG = 1;
    private final Map<String, String> userIdToUserName = new ConcurrentHashMap<>();
    private final Map<String, Conversation> channelsCache = new ConcurrentHashMap<>();
    private final List<String> trashSymbols = Arrays.asList("@", "<", ">");
    private final List<String> trashMsgs = Arrays.asList(LIMITS_MSG, BOT_IS_TYPING, THE_ANSWER_IS_HUGE_MSG, HAS_JOINED_MSG);
    @Value("${chat.history.limit:10}")
    private Integer historyLimit;
    @Value("${openai.qa.threshold.soft:3000}")
    private long softThresholdMs;
    @Value("${openai.qa.threshold.hard:10000}")
    private long hardThresholdMs;

    private final TokenUsageService tokenUsageService;

    @Override
    public List<Message> getMsgHistory(QuestionDto dto, ContextDto ctxDto) throws SlackApiException, IOException {
        var history = getHistory(ctxDto);
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
        var replies = getReplies(ctxDto, threadHeadMsg.getTs());
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

    @Override
    public boolean isCorrectToAnswerMsg(String text, String botUserId, boolean isIm) {
        if (isIm) {
            return isNotBlank(text);
        }
        return StringUtils.contains(text, String.format("<@%s>", botUserId));
    }

    @Override
    public String cleanupMessage(String input) {
        if (trashSymbols.stream().anyMatch(input::contains)) {
            return StringUtils.trim(input.replaceAll("<@.*?>", ""));
        }
        return input;
    }

    @Override
    public List<LayoutBlock> wrapInBlock(String respText) {
        return Collections.singletonList(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(respText)
                        .build())
                .build());
    }

    @Override
    public boolean isHardThresholdExceeded(ContextDto contextDto,
                                           QuestionDto dto,
                                           Conversation channelInfo)
            throws SlackApiException, IOException {
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
                log.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
            }
            return true;
        }
        return false;
    }

    @Override
    public String getCachedUserFullName(String user, MethodsClient client, String botToken) throws SlackApiException, IOException {
        if (!userIdToUserName.containsKey(user)) {
            String userFullName = getUserFullName(user, client, botToken);
            userIdToUserName.put(user, userFullName);
        }
        return userIdToUserName.get(user);
    }

    @Override
    public Conversation getCachedChannelInfo(ContextDto contextDto) throws SlackApiException, IOException {
        String channel = contextDto.channelId();
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        if (!channelsCache.containsKey(channel)) {
            Conversation channelInfo = getChannelInfo(client, channel, botToken);
            if (isNull(channelInfo)) {
                log.debug("Channel info is empty");
                return null;
            }
            channelsCache.put(channel, channelInfo);
        }
        return channelsCache.get(channel);
    }

    @Override
    public ContextDto buildContext(Context ctx) {
        String botToken = ctx.getBotToken();
        String botUserId = ctx.getBotUserId();
        String channelId = null;
        if (ctx instanceof EventContext eCtx) {
            channelId = eCtx.getChannelId();
        }
        MethodsClient client = ctx.client();
        return new ContextDto(botToken, client, botUserId, channelId,
                softThresholdMs, hardThresholdMs);
    }

    @Override
    public boolean isValidQuestion(ContextDto contextDto, QuestionDto dto) throws SlackApiException, IOException {
        log.debug("Context dto: {}", contextDto);
        Conversation channelInfo = this.getCachedChannelInfo(contextDto);
        if (isNull(channelInfo)) {
            log.warn(NO_CHAT_ACCESS, contextDto.channelId());
            return false;
        }
        String botUserId = contextDto.botUserId();
        if (!this.isCorrectToAnswerMsg(dto.getRawInputText(), botUserId, channelInfo.isIm())) {
            log.info("Bot not tagged or isn't an IM channel, ignore");
            return false;
        }
        if (this.isHardThresholdExceeded(contextDto, dto, channelInfo)) {
            return false;
        }
        return true;
    }


    private ConversationsHistoryResponse getHistory(ContextDto ctxDto)
            throws IOException, SlackApiException {
        var historyRequest = ConversationsHistoryRequest.builder()
                .channel(ctxDto.channelId())
                .limit(ONLY_LAST_THREAD_MSG)
                .token(ctxDto.botToken())
                .inclusive(true)
                .includeAllMetadata(true)
                .build();
        return ctxDto.client().conversationsHistory(historyRequest);
    }

    private ConversationsRepliesResponse getReplies(ContextDto ctxDto, String threadMsgTs)
            throws IOException, SlackApiException {
        ConversationsRepliesRequest request = ConversationsRepliesRequest.builder()
                .token(ctxDto.botToken())
                .limit(historyLimit)
                .channel(ctxDto.channelId())
                .ts(threadMsgTs)
                .build();
        return ctxDto.client().conversationsReplies(request);
    }

    private boolean isNotTechMsg(Message x) {
        return trashMsgs.stream().noneMatch(tm -> StringUtils.contains(x.getText(), tm));
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

}
