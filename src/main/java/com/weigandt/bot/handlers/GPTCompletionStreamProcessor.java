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
import com.weigandt.bot.ContextDto;
import com.weigandt.bot.QuestionDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.chatsettings.dto.TokenUsageDto;
import com.weigandt.chatsettings.service.TokenUsageService;
import com.weigandt.history.ChatHistoryLogService;
import com.weigandt.history.LogMsgJsonBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.weigandt.Constants.SEARCH.BOT_IS_TYPING;
import static com.weigandt.Constants.SEARCH.CANT_ANSWER_MSG;
import static com.weigandt.Constants.SEARCH.LIMITS_MSG;
import static com.weigandt.Constants.SEARCH.THE_ANSWER_IS_HUGE_MSG;
import static com.weigandt.Constants.SLACK_BOT.CHANNEL;
import static com.weigandt.Constants.SLACK_BOT.CHAT_POST_MESSAGE_FAILED;
import static com.weigandt.Constants.SLACK_BOT.IM;
import static org.apache.commons.lang3.StringUtils.SPACE;

@RequiredArgsConstructor
public class GPTCompletionStreamProcessor {

    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;
    private final TokenUsageService tokenUsageService;
    private final ContextDto contextDto;
    private final Conversation channelInfo;
    private final QuestionDto dto;
    private final StringBuilder sb = new StringBuilder();
    private long startTimeMillis = System.currentTimeMillis();
    private boolean isTypingSent = false;

    public void initAnswering() throws IOException, SlackApiException {
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        Logger logger = contextDto.logger();
        ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                .channel(channelInfo.getId())
                .threadTs(dto.getThreadTs())
                .token(botToken)
                .blocks(slackSupportService.wrapInBlock(BOT_IS_TYPING))
        );

        if (!messageResponse.isOk()) {
            logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
        }
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
        if (startTimeMillis != 0 && (currentTimeMillis - startTimeMillis) > contextDto.hardThresholdMs()) {
            shouldWaitForAnswer();
            startTimeMillis = 0;
        } else if (!isTypingSent && startTimeMillis != 0 && (currentTimeMillis - startTimeMillis) > contextDto.softThresholdMs()) {
            initAnswering();
            isTypingSent = true;
        }

        Optional<String> finishReason = chatCompletionChunk
                .getChoices().stream()
                .map(ChatCompletionChoice::getFinishReason)
                .filter(StringUtils::isNotBlank).findFirst();
        if (finishReason.isPresent()) {
            logger.debug("Stream finish reason: {}", finishReason.get());
            String respText = String.format("<@%s> %s %s", dto.getUser(), dto.getAnswerPrefix(), sb);
            RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder> postMsgConfigurator = r -> r
                    .channel(channelInfo.getId())
                    .threadTs(dto.getThreadTs())
                    .token(botToken)
                    .blocks(slackSupportService.wrapInBlock(respText));
            ChatPostMessageResponse messageResponse = client.chatPostMessage(postMsgConfigurator);
            if (!messageResponse.isOk()) {
                logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
                return;
            }
            String userFullName = slackSupportService.getCachedUserFullName(dto.getUser(), client, botToken);
            int questionSymbolsCount = StringUtils.length(dto.getQuestion());
            int answerSymbolsCount = StringUtils.length(sb);
            LogMsgJsonBuilder logEntry = LogMsgJsonBuilder.builder()
                    .datetime(String.valueOf(System.currentTimeMillis()))
                    .userName(userFullName)
                    .channelId(channelInfo.getId())
                    .channelName(channelInfo.getName())
                    .channelType(getChannelType(channelInfo))
                    .question(dto.getQuestion())
                    .answer(sb.toString())
                    .questionSymbolsCount(questionSymbolsCount)
                    .answerSymbolsCount(answerSymbolsCount)
                    .build();
            chatHistoryLogService.logCommunicationForUser(userFullName, logEntry);
            TokenUsageDto tokenUsageDto = new TokenUsageDto(userFullName, questionSymbolsCount, answerSymbolsCount);
            tokenUsageService.saveStatistics(tokenUsageDto);
            checkSoftThreshold(userFullName);
        }
    }

    private void checkSoftThreshold(String userName) throws SlackApiException, IOException {
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        Logger logger = contextDto.logger();
        if (tokenUsageService.isSoftThresholdExceeded(userName)) {
            Integer tokenUsedTotal = tokenUsageService.getTodayStatistics(userName).get().getTokenUsedTotal();
            Integer threshold = tokenUsageService.getUserTokenRestriction(userName).hardThreshold();
            String respText = String.format(LIMITS_MSG + " - not more than %s symbols per day. Symbols spent: %s", threshold, tokenUsedTotal);
            RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder> postMsgConfigurator = r -> r
                    .channel(channelInfo.getId())
                    .threadTs(dto.getThreadTs())
                    .token(botToken)
                    .blocks(slackSupportService.wrapInBlock(respText));
            ChatPostMessageResponse messageResponse = client.chatPostMessage(postMsgConfigurator);
            if (!messageResponse.isOk()) {
                logger.error(CHAT_POST_MESSAGE_FAILED, messageResponse.getError());
            }
        }
    }

    public void processException(Throwable e) throws SlackApiException, IOException {
        MethodsClient client = contextDto.client();
        String botToken = contextDto.botToken();
        Logger logger = contextDto.logger();

        logger.error("Something went wrong asking OpenAI for answer: {}", e.getMessage(), e);
        ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                .channel(channelInfo.getId())
                .threadTs(dto.getThreadTs())
                .token(botToken)
                .blocks(slackSupportService.wrapInBlock(CANT_ANSWER_MSG))
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
                .threadTs(dto.getThreadTs())
                .token(botToken)
                .blocks(slackSupportService.wrapInBlock(THE_ANSWER_IS_HUGE_MSG))
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
