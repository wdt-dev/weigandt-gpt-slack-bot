package com.weigandt.openai.impl;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.weigandt.openai.GPTQuestionService;
import io.reactivex.Flowable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.weigandt.Constants.OPENAI.ERROR_ANSWERING_QUESTION;
import static com.weigandt.Constants.OPENAI.FEATURE_NOT_ENABLED_MSG;
import static com.weigandt.Constants.SEARCH.PARAMS.CHAT_HISTORY;
import static com.weigandt.Constants.SEARCH.PARAMS.CONTEXT;
import static com.weigandt.Constants.SEARCH.PARAMS.QUESTION;
import static com.weigandt.Constants.SEARCH.QA_PROMPT;
import static com.weigandt.Constants.SEARCH.QA_WITH_HISTORY_PROMPT;
import static com.weigandt.Constants.SEARCH.REPHRASE_PROMPT;
import static java.util.Collections.singletonList;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultGPTQuestionService implements GPTQuestionService {
    @Value("${openai.qa.model:}")
    private String qaModel;
    @Value("${openai.qa.threshold.soft:3000}")
    private long softThresholdMs;
    @Value("${openai.qa.threshold.hard:10000}")
    private long hardThresholdMs;
    private final OpenAiService openAiService;
    private final Environment environment;

    @Override
    public String rephraseQuestion(String question, List<Message> chatHistory, String botUserId) {
        if (useEmbeddingsDisabled()) {
            log.warn(FEATURE_NOT_ENABLED_MSG);
            return null;
        }
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(getQaModel())
                .temperature(.0)
                .messages(prepareQuestion(question, chatHistory, botUserId, REPHRASE_PROMPT))
                .build();
        try {
            ChatCompletionResult completion = openAiService.createChatCompletion(request);
            return getFirstMessage(completion);
        } catch (Exception ex) {
            log.warn(ERROR_ANSWERING_QUESTION, question, ex);
        }
        return "There is no answer to your question";
    }

    @Override
    public String ask(String question) {
        if (StringUtils.isBlank(question)) {
            log.warn("Empty question is not a target to answer");
            return "I can't answer to empty questions";
        }
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(getQaModel())
                .temperature(.0)
                .messages(singletonList(createUserChatMessage(question)))
                .build();
        try {
            ChatCompletionResult completion = openAiService.createChatCompletion(request);
            return getFirstMessage(completion);
        } catch (Exception ex) {
            log.warn(ERROR_ANSWERING_QUESTION, question, ex);
        }
        return "There is no answer to your question";
    }

    @Override
    public Flowable<ChatCompletionChunk> askAsync(String question, List<Message> chatHistory,
                                                  String botUserId) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(getQaModel())
                .temperature(.0)
                .messages(prepareQuestion(question, chatHistory, botUserId, QA_WITH_HISTORY_PROMPT))
                .build();
        try {
            return openAiService.streamChatCompletion(request);
        } catch (Exception ex) {
            log.warn(ERROR_ANSWERING_QUESTION, question, ex);
        }
        return Flowable.empty();
    }

    @Override
    public String askWithExtras(String question, List<String> extras) {
        if (useEmbeddingsDisabled()) {
            log.warn(FEATURE_NOT_ENABLED_MSG);
            return null;
        }
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(getQaModel())
                .temperature(.0)
                .messages(prepareQuestionWithExtras(question, extras))
                .build();
        ChatCompletionResult completion = openAiService.createChatCompletion(request);
        return getFirstMessage(completion);
    }

    @Override
    public Flowable<ChatCompletionChunk> askWithExtrasAsync(String question, List<String> extras) {
        if (useEmbeddingsDisabled()) {
            log.warn(FEATURE_NOT_ENABLED_MSG);
            return Flowable.empty();
        }
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(getQaModel())
                .temperature(.0)
                .messages(prepareQuestionWithExtras(question, extras))
                .build();
        try {
            return openAiService.streamChatCompletion(request);
        } catch (Exception ex) {
            log.warn(ERROR_ANSWERING_QUESTION, question, ex);
        }
        return Flowable.empty();
    }

    @Override
    public long getSoftThresholdMs() {
        return softThresholdMs;
    }

    @Override
    public long getHardThresholdMs() {
        return hardThresholdMs;
    }

    private boolean useEmbeddingsDisabled() {
        return !ArrayUtils.contains(environment.getActiveProfiles(), "use-embeddings");
    }

    private List<ChatMessage> prepareQuestion(String question, List<Message> chatHistory,
                                              String botUserId,
                                              String template) {
        if (CollectionUtils.isEmpty(chatHistory)) {
            return singletonList(createUserChatMessage(question));
        }
        String transformedChatHistory = chatHistory.stream()
                .sorted(Comparator.comparing(Message::getTs))
                .map(msg -> transformMessage(msg, botUserId))
                .collect(Collectors.joining(System.lineSeparator()));

        String msg = template.replace(QUESTION, question)
                .replace(CHAT_HISTORY, transformedChatHistory);
        log.info("Full Question: \n{}", msg);
        return singletonList(createUserChatMessage(msg));
    }

    private String transformMessage(Message message, String botUserId) {
        return (StringUtils.equals(botUserId, message.getUser()) ?
                ChatMessageRole.ASSISTANT.value() :
                ChatMessageRole.USER.value()) + ": " + message.getText();
    }

    private String getFirstMessage(ChatCompletionResult completion) {
        return completion.getChoices().get(0).getMessage().getContent();
    }

    private ChatMessage createUserChatMessage(String question) {
        return new ChatMessage(ChatMessageRole.USER.value(), question);
    }

    private List<ChatMessage> prepareQuestionWithExtras(String question, List<String> extras) {
        String context = extras.stream().collect(Collectors.joining(System.lineSeparator()));

        String msg = QA_PROMPT.replace(QUESTION, question)
                .replace(CONTEXT, context);
        return singletonList(createUserChatMessage(msg));
    }
}