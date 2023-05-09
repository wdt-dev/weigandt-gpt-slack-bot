package com.weigandt.answering;

import com.slack.api.model.Message;
import com.weigandt.openai.EmbeddingsService;
import com.weigandt.openai.GPTQuestionService;
import com.weigandt.pinecone.VectorSearchService;
import io.pinecone.proto.SingleQueryResults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final GPTQuestionService gptQuestionService;
    private final EmbeddingsService embeddingsService;
    private final VectorSearchService vectorSearchService;

    public String getAnswer(String question, List<Message> chatHistory) {
        // 1 - reformat question (with chat history)
        String rephrasedQuestion = gptQuestionService.rephraseQuestion(question, chatHistory);

        //  2 - create embedding from question
        List<Float> questionEmbedding = embeddingsService.createEmbeddingsForPinecone(rephrasedQuestion);

        //  3 - get vectors with data
        SingleQueryResults vectorsResult = vectorSearchService.search(questionEmbedding);
        List<String> textBlocksFromVectorStorage = vectorSearchService.getTextBlocks(vectorsResult);

        //  4 - ask GPT-4 for answer with data from vectors
        return gptQuestionService.askWithExtras(rephrasedQuestion, textBlocksFromVectorStorage);
    }
}
