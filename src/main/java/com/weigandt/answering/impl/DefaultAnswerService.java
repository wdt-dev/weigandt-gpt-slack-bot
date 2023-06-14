package com.weigandt.answering.impl;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.answering.AnswerService;
import com.weigandt.openai.EmbeddingsService;
import com.weigandt.openai.GPTQuestionService;
import com.weigandt.pinecone.VectorSearchService;
import io.pinecone.proto.SingleQueryResults;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAnswerService implements AnswerService {

    private final GPTQuestionService gptQuestionService;
    private final EmbeddingsService embeddingsService;
    private final VectorSearchService vectorSearchService;

    @Override
    public Flowable<ChatCompletionChunk> getExtrasAnswerWithStreaming(String question,
                                                                      List<Message> chatHistory,
                                                                      String botUserId) {
        // 1 - reformat question (with chat history)
        String rephrasedQuestion = gptQuestionService.rephraseQuestion(question, chatHistory, botUserId);

        //  2 - create embedding from question
        List<Float> questionEmbedding = embeddingsService.createEmbeddingsForPinecone(rephrasedQuestion);

        //  3 - get vectors with data
        SingleQueryResults vectorsResult = vectorSearchService.search(questionEmbedding);
        List<String> textBlocksFromVectorStorage = vectorSearchService.getTextBlocks(vectorsResult);

        //  4 - ask GPT-4 for answer with data from vectors
        return gptQuestionService.askWithExtrasStream(rephrasedQuestion, textBlocksFromVectorStorage);
    }

    @Override
    public String getDefaultGptAnswer(String question) {
        return gptQuestionService.ask(question);
    }

    @Override
    public Flowable<ChatCompletionChunk> getAnswerWithStreaming(String question, List<Message> history, String botUserId) {
        return gptQuestionService.askWithStreaming(question, history, botUserId);
    }

    @Override
    public long getSoftThresholdMs() {
        return gptQuestionService.getSoftThresholdMs();
    }

    @Override
    public long getHardThresholdMs() {
        return gptQuestionService.getHardThresholdMs();
    }
}
