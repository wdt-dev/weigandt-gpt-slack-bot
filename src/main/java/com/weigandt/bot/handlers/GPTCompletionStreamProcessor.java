package com.weigandt.bot.handlers;

import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Conversation;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.weigandt.bot.EventDto;
import com.weigandt.bot.SlackSupportService;
import com.weigandt.history.ChatHistoryLogService;
import com.weigandt.history.LogMsgJsonBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.SPACE;

@RequiredArgsConstructor
public class GPTCompletionStreamProcessor {
    private final SlackSupportService slackSupportService;
    private final ChatHistoryLogService chatHistoryLogService;
    private final EventContext ctx;
    private final Conversation channelInfo;
    private final EventDto dto;
    private final StringBuilder sb = new StringBuilder();

    public void processChunks(ChatCompletionChunk chatCompletionChunk)
            throws SlackApiException, IOException {
        MethodsClient client = ctx.client();
        String botToken = ctx.getBotToken();
        Logger logger = ctx.logger;

        String answerChunk = chatCompletionChunk.getChoices().stream()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(SPACE));
        sb.append(answerChunk);

        Optional<String> finishReason = chatCompletionChunk
                .getChoices().stream()
                .map(ChatCompletionChoice::getFinishReason)
                .filter(StringUtils::isNotBlank).findFirst();
        if (finishReason.isPresent()) {
            logger.info("Stream finish reason: {}", finishReason.get());
            String respText = String.format("<@%s> %s", dto.getUser(), sb);
            ChatPostMessageResponse messageResponse = client.chatPostMessage(r -> r
                    .channel(channelInfo.getId())
                    .threadTs(dto.getThreadTs())
                    .token(botToken)
                    .blocks(slackSupportService.wrapInBlock(respText))
            );
            if (!messageResponse.isOk()) {
                logger.error("chat.postMessage failed: {}", messageResponse.getError());
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

    protected String getChannelType(Conversation channelInfo) {
        if (channelInfo.isIm()) {
            return "im";
        }
        if (channelInfo.isChannel()) {
            return "channel";
        }
        return "unknown";
    }
}
