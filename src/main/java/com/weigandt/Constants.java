package com.weigandt;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SLACK_BOT {
        public static final String CMD_HELLO = "/hello";
        public static final String CMD_EXTRAS = "/extras";
        public static final String CMD_IMPORT = "/import";
        public static final String LOG_EXT = ".log";
        public static final String CHANNEL = "channel";
        public static final String IM = "im";
        public static final String CHAT_POST_MESSAGE_FAILED = "chat.postMessage failed: {}";
        public static final String NO_CHAT_ACCESS = "The bot can't get channel info for channel id:{}, pls check bot access";
        public static final String TOKEN_LIMIT_EXCEEDED = "Your day limit exceeded, welcome back another day. Symbols spent: %s";
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OPENAI {
        public static final String FEATURE_NOT_ENABLED_MSG = "Feature not enabled, pls check your profiles";
        public static final String VARIOUS_TEXT = "variousText";
        public static final String ERROR_ANSWERING_QUESTION = "Error answering question: {}";
        public static final Integer DEFAULT_SOFT_RESTRICTION = 50000;
        public static final Integer DEFAULT_HARD_RESTRICTION = 100000;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PINECONE {
        public static final String F_SOURCE_TEXT = "sourceText";
        public static final String F_FILE_NAME = "fileName";

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FILE_PROCESSOR {
        public static final String PDF_EXT = ".pdf";
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SEARCH {
        public static final String QA_PROMPT = """
                        You are an AI assistant providing helpful advice. You are given the following extracted parts of a long document and a question. Provide a conversational answer based on the context provided.
                        You should only provide hyperlinks that reference the context below. Do NOT make up hyperlinks.
                        If you can't find the answer in the context below, just say "Hmm, I'm not sure." Don't try to make up an answer.
                        If the question is not related to the context, politely respond that you are tuned to only answer questions that are related to the context.

                        Question: {question}
                =========
                        {context}
                =========
                        Answer in Markdown:""";

        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static class PARAMS {
            public static final String CHAT_HISTORY = "{chat_history}";
            public static final String CONTEXT = "{context}";
            public static final String QUESTION = "{question}";
        }

        public static final String REPHRASE_PROMPT = """
                Given the following conversation and a follow up question, rephrase the follow up question to be a standalone question.

                Chat History:
                {chat_history}
                Follow Up Input: {question}
                Standalone question:""";

        public static final String QA_WITH_HISTORY_PROMPT = """
                Given the following conversation and a new question, please answer to last question of user:

                Chat History:
                {chat_history}
                User: {question}
                """;

    }
}