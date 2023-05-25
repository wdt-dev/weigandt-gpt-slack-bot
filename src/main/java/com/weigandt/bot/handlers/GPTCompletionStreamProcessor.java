package com.weigandt.bot.handlers;

import com.slack.api.RequestConfigurator;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Conversation;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.weigandt.bot.CommandDto;
import com.weigandt.bot.ContextDto;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.history.ChatHistoryLogService;
import com.weigandt.history.LogMsgJsonBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.weigandt.Constants.SLACK_BOT.CHANNEL;
import static com.weigandt.Constants.SLACK_BOT.IM;
import static org.apache.commons.lang3.StringUtils.SPACE;

@RequiredArgsConstructor
public class GPTCompletionStreamProcessor {
    private static final String CHAT_POST_MESSAGE_FAILED = "chat.postMessage failed: {}";
    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;
    private final ContextDto contextDto;
    private final Conversation channelInfo;
    private final CommandDto dto;
    private final StringBuilder sb = new StringBuilder();
    private long startTimeMillis = System.currentTimeMillis();
    private boolean isTypingSent = false;

    public void initAnswering() throws IOException, SlackApiException {
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        Logger logger = contextDto.logger();
        ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                .channel(channelInfo.getId())
                .threadTs(getThreadTs())
                .token(botToken)
                .blocks(slackSupportService.wrapInBlock("Bot is typing..."))
        );

        if (!messageResponse.isOk()) {
            logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
        }
    }

    @Nullable
    private String getThreadTs() {
        return dto instanceof EventDto eventDto ? eventDto.getThreadTs() : null;
    }

    public void processChunks(ChatCompletionChunk chatCompletionChunk)
            throws SlackApiException, IOException {
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        Logger logger = contextDto.logger();

        String answerChunk = chatCompletionChunk.getChoices().stream()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(SPACE));
        sb.append(answerChunk);
        long currentTimeMillis = System.currentTimeMillis();
        if (startTimeMillis != 0 && (currentTimeMillis - startTimeMillis) > 10000) {
            shouldWaitForAnswer();
            startTimeMillis = 0;
        } else if (!isTypingSent && startTimeMillis != 0 && (currentTimeMillis - startTimeMillis) > 3000) {
            initAnswering();
            isTypingSent = true;
        }

        Optional<String> finishReason = chatCompletionChunk
                .getChoices().stream()
                .map(ChatCompletionChoice::getFinishReason)
                .filter(StringUtils::isNotBlank).findFirst();
        if (finishReason.isPresent()) {
            logger.info("Stream finish reason: {}", finishReason.get());
            String respText = String.format("<@%s> %s", dto.getUser(), sb);
            RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder> postMsgConfigurator = r -> r
                    .channel(channelInfo.getId())
                    .threadTs(getThreadTs())
                    .token(botToken)
                    .blocks(slackSupportService.wrapInBlock(respText));
            ChatPostMessageResponse messageResponse = client.chatPostMessage(postMsgConfigurator);
            if (!messageResponse.isOk()) {
                logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
                return;
            }
            String userFullName = slackSupportService.getCachedUserFullName(dto.getUser(), client, botToken);
            LogMsgJsonBuilder logEntry = LogMsgJsonBuilder.builder()
                    .datetime(String.valueOf(System.currentTimeMillis()))
                    .userName(userFullName)
                    .channelId(channelInfo.getId())
                    .channelName(channelInfo.getName())
                    .channelType(getChannelType(channelInfo))
                    .question(dto.getQuestion())
                    .answer(sb.toString())
                    .questionSymbolsCount(StringUtils.length(dto.getQuestion()))
                    .answerSymbolsCount(StringUtils.length(sb))
                    .build();
            chatHistoryLogService.logCommunicationForUser(userFullName, logEntry);
        }
    }

    public void processException(Throwable e) throws SlackApiException, IOException {
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        Logger logger = contextDto.logger();

        logger.error("Something went wrong asking OpenAI for answer: {}", e.getMessage(), e);
        ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                .channel(channelInfo.getId())
                .threadTs(getThreadTs())
                .token(botToken)
                .blocks(slackSupportService.wrapInBlock("I tried to answer but I can't, please try again"))
        );
        if (!messageResponse.isOk()) {
            logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
        }
    }

    public void shouldWaitForAnswer() throws IOException, SlackApiException {
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        Logger logger = contextDto.logger();
        ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                .channel(channelInfo.getId())
                .threadTs(getThreadTs())
                .token(botToken)
                .blocks(slackSupportService.wrapInBlock("The answer is huge, please wait"))
        );

        if (!messageResponse.isOk()) {
            logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
        }
    }

    protected String getChannelType(Conversation channelInfo) {
        if (channelInfo.isIm()) {
            return IM;
        }
        if (channelInfo.isChannel()) {
            return CHANNEL;
        }
        return "unknown";
    }
}
