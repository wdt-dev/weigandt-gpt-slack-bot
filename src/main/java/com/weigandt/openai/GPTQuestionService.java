package com.weigandt.openai;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.weigandt.Constants.SEARCH.PARAMS.CHAT_HISTORY;
import static com.weigandt.Constants.SEARCH.PARAMS.CONTEXT;
import static com.weigandt.Constants.SEARCH.PARAMS.QUESTION;
import static com.weigandt.Constants.SEARCH.QA_PROMPT;
import static com.weigandt.Constants.SEARCH.REPHRASE_PROMPT;
import static com.weigandt.Constants.SLACK_BOT.BOT_ID;

@Service
@Getter
@RequiredArgsConstructor
public class GPTQuestionService {

    @Value("${openai.qa.model}")
    private String qaModel;
    private final OpenAiService openAiService;

    public String rephraseQuestion(String question, List<Message> chatHistory) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(getQaModel())
                .temperature(.0)
                .messages(prepareQuestion(question, chatHistory))
                .build();
        ChatCompletionResult completion = openAiService.createChatCompletion(request);
        return getFirstMessage(completion);
    }

    public String askWithExtras(String question, List<String> extras) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(getQaModel())
                .temperature(.0)
                .messages(prepareQuestionWithExtras(question, extras))
                .build();
        ChatCompletionResult completion = openAiService.createChatCompletion(request);
        return getFirstMessage(completion);
    }

    private List<ChatMessage> prepareQuestion(String question, List<Message> chatHistory) {
        String transformedChatHistory = chatHistory.stream()
                .sorted(Comparator.comparing(Message::getTs))
                .map(this::transformMessage)
                .collect(Collectors.joining(System.lineSeparator()));

        String msg = REPHRASE_PROMPT.replace(QUESTION, question)
                .replace(CHAT_HISTORY, transformedChatHistory);
        return Collections.singletonList(createUserChatMessage(msg));
    }

    private String transformMessage(Message message) {
        return (BOT_ID.equals(message.getUser()) ? "Answer" : "Question") + ": " + message.getText();
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
        return Collections.singletonList(createUserChatMessage(msg));
    }

}
