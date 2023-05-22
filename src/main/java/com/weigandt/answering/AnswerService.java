package com.weigandt.answering;

import com.slack.api.model.Message;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.weigandt.openai.EmbeddingsService;
import com.weigandt.openai.GPTQuestionService;
import com.weigandt.pinecone.VectorSearchService;
import io.pinecone.proto.SingleQueryResults;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerService {

    private final GPTQuestionService gptQuestionService;
    private final EmbeddingsService embeddingsService;
    private final VectorSearchService vectorSearchService;

    public String getAnswer(String question, List<Message> chatHistory, String botUserId) {
        if (StringUtils.isBlank(question)) {
            log.warn("Empty question is not a target to answer");
            return "I can't answer to empty questions";
        }
        // 1 - reformat question (with chat history)
        String rephrasedQuestion = gptQuestionService.rephraseQuestion(question, chatHistory, botUserId);

        //  2 - create embedding from question
        List<Float> questionEmbedding = embeddingsService.createEmbeddingsForPinecone(rephrasedQuestion);

        //  3 - get vectors with data
        SingleQueryResults vectorsResult = vectorSearchService.search(questionEmbedding);
        List<String> textBlocksFromVectorStorage = vectorSearchService.getTextBlocks(vectorsResult);

        //  4 - ask GPT-4 for answer with data from vectors
        return gptQuestionService.askWithExtras(rephrasedQuestion, textBlocksFromVectorStorage);
    }

    public String getDefaultGptAnswer(String question) {
        return gptQuestionService.ask(question);
    }

    public Flowable<ChatCompletionChunk> getAnswerWithStreaming(String question) {
        return gptQuestionService.askWithStreaming(question);
    }
}
